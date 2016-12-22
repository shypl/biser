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

import java.net.SocketAddress;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	private static final byte[] SID_SALT;
	
	private static final ThreadLocal<ByteBuffer> threadLocalMessageBuffer = ThreadLocal.withInitial(ByteBuffer::new);
	
	static {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		SID_SALT = new byte[random.nextInt(8, 17)];
		random.nextBytes(SID_SALT);
	}
	
	private final Observers<Consumer<AbstractClient>> disconnectObservers = new Observers<>();
	private final long id;
	private final OutgoingMessages outgoingMessages = new OutgoingMessages();
	
	private Logger logger = LOGGER;
	
	private volatile boolean connected;
	private volatile boolean active;
	
	private Server     server;
	private Connection connection;
	private Worker     worker;
	
	private Cancelable recoveryTimeout;
	private Connection recoveryConnection;
	private int        recoveryLastMessageId;
	
	private boolean disconnectForceSecondAttempt;
	private int     lastIncomingMessageId;
	
	
	public AbstractClient(long id) {
		this.id = id;
	}
	
	public final long getId() {
		return id;
	}
	
	public final SocketAddress getRemoteAddress() {
		return connection.getRemoteAddress();
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
				if (!disconnectForceSecondAttempt && active) {
					disconnectForceSecondAttempt = true;
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
	
	protected void handleErrorInProcessingIncomingMessages(Throwable e) {
		logger.error("Error on process message", e);
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
		worker = new Worker(server.getExecutorsProvider().getClientExecutorService());
		
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
	
	void reconnect(Connection connection, int lastMessageId) {
		worker.addTask(() -> {
			if (connected) {
				if (active) {
					recoveryConnection = connection;
					recoveryLastMessageId = lastMessageId;
					this.connection.send(Protocol.PING);
				}
				else {
					cancelRecoveryTimeout();
					
					active = true;
					this.connection = connection;
					connection.setProcessor(new ConnectionProcessorMessaging(this));
					
					if (connection.getLogger().isTraceEnabled()) {
						logger.debug("Reconnect (connectionId: {})", connection.getId());
					}
					
					byte[] sid = calculateSid();
					
					connection.send(new ByteBuffer(1 + 1 + 4 + 8 + sid.length)
						.writeByte(Protocol.RECOVERY)
						.writeByte((byte)(4 + 8 + sid.length))
						.writeInt(lastIncomingMessageId)
						.writeLong(id)
						.writeBytes(sid)
						.readBytes()
					);
					
					outgoingMessages.releaseTo(lastMessageId);
					for (OutgoingMessage message : outgoingMessages.getQueue()) {
						sendMessage0(message);
					}
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
				if (recoveryConnection == null) {
					recoveryTimeout = worker.scheduleTask(this::handleDisconnect, time, TimeUnit.SECONDS);
				}
				else {
					reconnect(recoveryConnection, recoveryLastMessageId);
					recoveryConnection = null;
				}
			}
		});
	}
	
	void receiveMessage(int messageId, byte[] bytes) {
		worker.addTask(() -> {
			lastIncomingMessageId = messageId;
			if (connected) {
				
				try {
					if (bytes.length == 0) {
						throw new IllegalArgumentException("Received message is empty");
					}
					
					server.getApi().processIncomingMessage(this, bytes);
				}
				catch (Throwable e) {
					try {
						handleErrorInProcessingIncomingMessages(e);
					}
					catch (Throwable e2) {
						logger.error("Error on process message", e);
						logger.error("Error in handle error in processing incoming messages", e2);
					}
					connection.close(ConnectionCloseReason.SERVER_ERROR);
				}
			}
			else {
				logger.debug("Fail receive message on disconnected");
			}
		});
	}
	
	void processOutgoingMessageReceived(int messageId) {
		worker.addTask(() -> outgoingMessages.releaseTo(messageId));
	}
	
	void sendMessage(byte[] bytes) {
		if (bytes.length == 0) {
			throw new IllegalArgumentException("Outgoing message is empty");
		}
		
		worker.addTask(() -> {
			if (connected) {
				OutgoingMessage message = outgoingMessages.create(bytes);
				if (active) {
					sendMessage0(message);
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
				connection.syncSend(getBufferWithLastMessageReceivedFlag()
					.writeBytes(bytes)
					.readBytesAndClear()
				);
			}
			else {
				logger.debug("Fail send data on disconnected");
			}
		});
	}
	
	void sendLastMessageReceivedFlag() {
		worker.addTask(() -> {
			if (connected) {
				connection.send(getBufferWithLastMessageReceivedFlag().readBytesAndClear());
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
	
	Server getServer() {
		return server;
	}
	
	private void sendMessage0(OutgoingMessage message) {
		connection.send(getBufferWithLastMessageReceivedFlag()
			.writeByte(Protocol.MESSAGE)
			.writeInt(message.id)
			.writeInt(message.data.length)
			.writeBytes(message.data)
			.readBytesAndClear()
		);
	}
	
	private ByteBuffer getBufferWithLastMessageReceivedFlag() {
		ByteBuffer buffer = threadLocalMessageBuffer.get();
		buffer.writeByte(Protocol.MESSAGE_RECEIVED);
		buffer.writeInt(lastIncomingMessageId);
		return buffer;
	}
	
	private void handleDisconnect() {
		active = false;
		connected = false;
		
		cancelRecoveryTimeout();
		
		try {
			onDisconnect();
		}
		catch (Throwable e) {
			logger.error("Error on disconnect", e);
		}
		
		server.disconnectClient(this);
		server.getApi().informClientDisconnectObservers(this);
		
		disconnectObservers.inform(observer -> observer.accept(this));
		disconnectObservers.removeAll();
	}
	
	private void cancelRecoveryTimeout() {
		if (recoveryTimeout != null) {
			recoveryTimeout.cancel();
			recoveryTimeout = null;
		}
	}
}
