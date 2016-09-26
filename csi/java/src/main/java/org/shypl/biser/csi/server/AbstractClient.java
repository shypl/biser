package org.shypl.biser.csi.server;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.ConnectionCloseReason;
import org.shypl.biser.csi.Protocol;
import org.shypl.common.concurrent.Worker;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.shypl.common.util.Cancelable;
import org.shypl.common.util.Observers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	private static final byte[] SID_SALT;

	private static final ThreadLocal<ByteBuffer> threadLocalMessageBuffer = new ThreadLocal<ByteBuffer>() {
		@Override
		protected ByteBuffer initialValue() {
			return new ByteBuffer();
		}
	};

	static {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		SID_SALT = new byte[random.nextInt(8, 17)];
		random.nextBytes(SID_SALT);
	}

	private final Observers<Consumer<AbstractClient>> disconnectObservers = new Observers<>();
	private final long   id;
	private       Logger logger = LOGGER;

	private volatile boolean connected;
	private volatile boolean active;

	private Server     server;
	private Connection connection;
	private Worker     worker;

	private Connection connectionForRecovery;
	private Cancelable connectionRecoveryTimeout;

	private boolean inputMessageEven;
	private boolean outputMessageEven;
	private boolean       outputMessageSendAvailable = true;
	private Deque<byte[]> outputMessages             = new LinkedList<>();


	public AbstractClient(long id) {
		this.id = id;
	}

	public final long getId() {
		return id;
	}

	public final Worker getWorker() {
		return worker;
	}

	public boolean isConnected() {
		return connected;
	}

	public final Cancelable addDisconnectObserver(Consumer<AbstractClient> observer) {
		return disconnectObservers.add(observer);
	}

	public final void removeDisconnectObserver(Consumer<AbstractClient> observer) {
		disconnectObservers.remove(observer);
	}

	public final void disconnect() {
		disconnect(ConnectionCloseReason.NONE);
	}

	public final void disconnect(ConnectionCloseReason reason) {
		disconnect(reason, (Consumer<AbstractClient>)null);
	}

	public final void disconnect(ConnectionCloseReason reason, Consumer<AbstractClient> callback) {
		worker.addTask(() -> {
			if (connected) {
				if (callback != null) {
					disconnectObservers.add(callback);
				}
				if (active) {
					connection.close(reason);
				}
				else {
					handleDisconnect();
				}
			}
			else if (callback != null) {
				callback.accept(this);
			}
		});
	}

	public final void disconnect(ConnectionCloseReason reason, Runnable callback) {
		disconnect(reason, client -> callback.run());
	}

	protected void onConnect() {
	}

	protected void onDisconnect() {
	}

	void init(String apiName) {
		logger = new PrefixedLoggerProxy(LOGGER, '[' + apiName + '#' + id + "] ");
	}

	Logger getLogger() {
		return logger;
	}

	void connect(Server server, Connection connection) {
		connected = true;
		active = true;

		this.server = server;
		this.connection = connection;
		worker = new Worker(server.getConnectionsExecutor());

		worker.addTask(() -> {
			byte[] sid = calculateSid();

			connection.setProcessor(new ConnectionProcessorMessaging(this));

			if (connection.getLogger().isTraceEnabled()) {
				logger.debug("Connect (connectionId: {}, sid: {})", connection.getId(), Hex.encodeHexString(sid));
			}

			connection.send(new ByteBuffer(1 + 4 + 4 + 1 + 8 + sid.length)
				.writeByte(Protocol.AUTHORIZATION)
				.writeInt(server.getSettings().getConnectionActivityTimeout())
				.writeInt(server.getSettings().getConnectionRecoveryTimeout())
				.writeByte((byte)(8 + sid.length))
				.writeLong(id)
				.writeBytes(sid)
				.readBytes()
			);

			try {
				onConnect();
			}
			catch (Throwable e) {
				logger.error("Error on connect", e);
				disconnect(ConnectionCloseReason.SERVER_ERROR);
				return;
			}

			server.getApi().informClientConnectObservers(this);
		});
	}

	void reconnect(Connection connection) {
		worker.addTask(() -> {
			if (connected) {
				if (active) {
					connectionForRecovery = connection;
					this.connection.send(Protocol.PING);
				}
				else {
					cancelConnectionRecoveryTimeout();

					active = true;
					this.connection = connection;
					connection.setProcessor(new ConnectionProcessorMessaging(this));

					if (connection.getLogger().isTraceEnabled()) {
						logger.debug("Reconnect (connectionId: {})", connection.getId());
					}

					byte[] sid = calculateSid();

					connection.send(new ByteBuffer(1 + 1 + 8 + sid.length + 1)
						.writeByte(Protocol.RECOVERY)
						.writeByte((byte)(8 + sid.length))
						.writeLong(id)
						.writeBytes(sid)
						.writeByte(inputMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED)
						.readBytes()
					);
				}
			}
			else {
				logger.debug("Fail reconnect on disconnected");
			}
		});
	}

	void handleConnectionClosed() {
		worker.addTask(this::handleDisconnect);
	}

	void handleConnectionBreaking() {
		worker.addTask(() -> {
			active = false;
			int time = server.getSettings().getConnectionRecoveryTimeout();
			if (time <= 0) {
				handleDisconnect();
			}
			else {
				if (connectionForRecovery == null) {
					connectionRecoveryTimeout = worker.scheduleTask(this::handleDisconnect, time, TimeUnit.SECONDS);
				}
				else {
					reconnect(connectionForRecovery);
					connectionForRecovery = null;
				}
			}
		});
	}

	void receiveMessage(boolean even, byte[] bytes) {
		worker.addTask(() -> {
			if (connected) {
				if (inputMessageEven == even) {
					logger.error("Violation of the Message Queuing");
				}
				inputMessageEven = even;
				connection.send(inputMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);

				try {
					if (bytes.length == 0) {
						throw new IllegalArgumentException("Received message is empty");
					}

					server.getApi().processIncomingMessage(this, bytes);
				}
				catch (Throwable e) {
					logger.error("Error on process message", e);
					connection.close(ConnectionCloseReason.SERVER_ERROR);
				}
			}
			else {
				logger.debug("Fail receive message on disconnected");
			}
		});
	}

	void sendMessage(byte[] bytes) {
		worker.addTask(() -> {
			if (connected) {
				outputMessages.addLast(bytes);
				if (active && outputMessageSendAvailable) {
					sendMessage0(bytes);
				}
			}
			else {
				logger.debug("Fail send message on disconnected");
			}
		});
	}

	void sendData(byte[] bytes) {
		worker.addTask(() -> {
			if (connected) {
				connection.send(bytes);
			}
			else {
				logger.debug("Fail send data on disconnected");
			}
		});
	}

	void processMessageReceived(boolean even) {
		worker.addTask(() -> {
			outputMessageSendAvailable = true;
			if (active) {
				if (outputMessageEven != even) {
					sendMessage0(outputMessages.getFirst());
				}
				else {
					outputMessages.pollFirst();
					if (!outputMessages.isEmpty()) {
						sendMessage0(outputMessages.getFirst());
					}
				}
			}
		});
	}

	byte[] calculateSid() {
		return DigestUtils.sha1(
			new ByteBuffer(8 + 8 + SID_SALT.length)
				.writeLong(id)
				.writeLong(connection.getId())
				.writeBytes(SID_SALT)
				.readBytes()
		);
	}

	private void sendMessage0(byte[] bytes) {
		if (bytes.length == 0) {
			throw new IllegalArgumentException("Outgoing message is empty");
		}

		outputMessageSendAvailable = false;
		outputMessageEven = !outputMessageEven;

		ByteBuffer buffer = threadLocalMessageBuffer.get();

		connection.send(buffer
			.writeByte(outputMessageEven ? Protocol.MESSAGE_EVEN : Protocol.MESSAGE_ODD)
			.writeInt(bytes.length)
			.writeBytes(bytes)
			.readBytes()
		);
	}

	private void handleDisconnect() {
		active = false;
		connected = false;

		cancelConnectionRecoveryTimeout();

		server.disconnectClient(this);
		server.getApi().informClientDisconnectObservers(this);

		disconnectObservers.inform(observer -> observer.accept(this));

		try {
			onDisconnect();
		}
		catch (Throwable e) {
			logger.error("Error on disconnect", e);
		}

		disconnectObservers.removeAll();
	}

	private void cancelConnectionRecoveryTimeout() {
		if (connectionRecoveryTimeout != null) {
			connectionRecoveryTimeout.cancel();
			connectionRecoveryTimeout = null;
		}
	}
}
