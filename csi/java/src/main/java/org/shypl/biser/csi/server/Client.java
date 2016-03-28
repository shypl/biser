package org.shypl.biser.csi.server;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.ConnectionCloseReason;
import org.shypl.biser.csi.Protocol;
import org.shypl.common.concurrent.Worker;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.shypl.common.util.Cancelable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public abstract class Client {
	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
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

	private final Queue<Runnable> disconnectObservers = new ConcurrentLinkedQueue<>();
	private final long   id;
	private       Logger logger;

	private volatile boolean connected;
	private volatile boolean active;

	private Server     server;
	private Connection connection;
	private Worker     worker;

	private Cancelable connectionRecoveryTimeout;

	private boolean inputMessageEven;
	private boolean outputMessageEven;
	private boolean       outputMessageSendAvailable = true;
	private Deque<byte[]> outputMessages             = new LinkedList<>();

	public Client(long id) {
		this.id = id;
	}

	public final long getId() {
		return id;
	}

	public final void observeDisconnect(Runnable observer) {
		disconnectObservers.add(observer);
	}

	public final void disconnect() {
		disconnect(ConnectionCloseReason.NONE);
	}

	public final void disconnect(ConnectionCloseReason reason) {
		disconnect(reason, null);
	}

	public final void disconnect(ConnectionCloseReason reason, Runnable callback) {
		worker.addTask(() -> {
			if (connected) {
				if (callback != null) {
					observeDisconnect(callback);
				}
				if (active) {
					connection.close(reason);
				}
				else {
					handleDisconnect();
				}
			}
			else if (callback != null) {
				callback.run();
			}
		});
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
		worker = new Worker(server.getExecutor());

		worker.addTask(() -> {
			byte[] sid = calculateSid();

			connection.setProcessor(new ConnectionProcessorMessaging(this));

			if (connection.getLogger().isTraceEnabled()) {
				logger.debug("Connect (connectionId: {}, sid: {})", id, connection.getId(), Hex.encodeHexString(sid));
			}

			connection.send(
				new ByteBuffer(1 + 8 + 1 + sid.length + 4)
					.writeByte(Protocol.AUTHORIZATION)
					.writeInt(server.getSettings().getConnectionActivityTimeout())
					.writeInt(server.getSettings().getConnectionRecoveryTimeout())
					.writeByte((byte)(sid.length + 8))
					.writeLong(id)
					.writeBytes(sid)
					.readBytes()
			);

			onConnect();
		});
	}

	void reconnect(Connection connection) {
		worker.addTask(() -> {
			if (connected) {
				cancelConnectionRecoveryTimeout();

				active = true;
				this.connection = connection;
				connection.setProcessor(new ConnectionProcessorMessaging(this));

				if (connection.getLogger().isTraceEnabled()) {
					logger.debug("Reconnect (connectionId: {})", id, connection.getId());
				}

				connection.send(new byte[]{
					Protocol.RECOVERY,
					inputMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED});

			}
			else {
				logger.warn("Fail reconnect on disconnected");
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
				connectionRecoveryTimeout = worker.scheduleTask(this::handleDisconnect, time, TimeUnit.SECONDS);
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
					server.getApi().processIncomingMessage(this, bytes);
				}
				catch (Throwable e) {
					logger.error("Error on process message", e);
					connection.close(ConnectionCloseReason.SERVER_ERROR);
				}
			}
			else {
				logger.warn("Fail receive message on disconnected");
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
				logger.warn("Fail send message on disconnected");
			}
		});
	}

	void sendData(byte[] bytes) {
		worker.addTask(() -> {
			if (connected) {
				connection.send(bytes);
			}
			else {
				logger.warn("Fail send data on disconnected");
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

		try {
			onDisconnect();
		}
		catch (Throwable e) {
			logger.error("Error on disconnect", e);
		}

		for (Runnable observer : disconnectObservers) {
			try {
				observer.run();
			}
			catch (Throwable e) {
				logger.error("Error on disconnect observer", e);
			}
		}
		disconnectObservers.clear();
	}

	private void cancelConnectionRecoveryTimeout() {
		if (connectionRecoveryTimeout != null) {
			connectionRecoveryTimeout.cancel();
			connectionRecoveryTimeout = null;
		}
	}
}
