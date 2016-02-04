package org.shypl.biser.csi.server;

import org.apache.commons.codec.binary.Hex;
import org.shypl.biser.csi.ByteArrayBuilder;
import org.shypl.biser.csi.Protocol;
import org.shypl.common.concurrent.ScheduledTask;
import org.shypl.common.concurrent.TaskQueue;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

	private final long                id;
	private final PrefixedLoggerProxy logger;

	private Connection connection;
	private long       connectionId;
	private CsiServer  server;
	private TaskQueue  taskQueue;

	private volatile boolean connected = true;
	private volatile boolean active    = false;
	private List<Runnable> disconnectHandlers;
	private ScheduledTask  reconnectTimeout;
	private boolean        receiveMessageEven;
	private boolean        sendMessageEven;
	private boolean       sendMessageReady = true;
	private Deque<byte[]> messages         = new LinkedList<>();

	public AbstractClient(long id) {
		this.id = id;
		logger = new PrefixedLoggerProxy(LOGGER, "<" + id + "> ");
	}

	public final long getId() {
		return id;
	}

	public final long getConnectionId() {
		return connectionId;
	}

	public final boolean isConnected() {
		return connected;
	}

	public final Logger getLogger() {
		return logger;
	}

	public final void addDisconnectHandler(Runnable handler) {
		addTask(() -> {
			if (disconnectHandlers == null) {
				disconnectHandlers = new ArrayList<>(1);
			}
			disconnectHandlers.add(handler);
		});
	}

	public final void addTask(Runnable task) {
		taskQueue.add(() -> {
			if (connected) {
				task.run();
			}
		});
	}

	protected void handleConnect() {
	}

	protected void handleDisconnect() {
	}

	boolean isActive() {
		return active;
	}

	Connection getConnection() {
		return connection;
	}

	void setServer(CsiServer server) {
		if (this.server != null) {
			throw new IllegalStateException();
		}
		this.server = server;
		taskQueue = new TaskQueue(server.getExecutor());
	}

	@SuppressWarnings("unchecked")
	void receiveMessage(boolean even, byte[] data) {
		addTask(() -> {
			receiveMessageEven = even;
			connection.write(receiveMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);
			server.getGate().processMessage(this, data);
		});
	}

	void sendMessage(byte[] data) {
		addTask(() -> {
			messages.addLast(data);
			if (active && sendMessageReady) {
				sendMessage0(data);
			}
		});
	}

	void completeMessageSend(boolean even) {
		taskQueue.add(() -> {
			sendMessageReady = true;
			if (connected) {
				if (sendMessageEven != even) {
					sendMessage0(messages.getFirst());
				}
				else {
					messages.pollFirst();
					if (!messages.isEmpty()) {
						sendMessage0(messages.getFirst());
					}
				}
			}
		});
	}

	void secConnection(Connection connection) {
		addTask(() -> {
			active = true;
			this.connection = connection;
			connectionId = connection.getId();
			boolean reconnect = cancelReconnectTimeout();
			byte[] sid = server.getClientConnectionSid(this);

			connection.setStrategy(new ConnectionStrategyMessaging(this));

			if (connection.getLogger().isTraceEnabled()) {
				connection.getLogger()
					.trace("Client: Connection established (clientId: {}, connectionId: {}, sid: {})", id, connectionId, Hex.encodeHexString(sid));
			}

			connection.write(
				new ByteArrayBuilder(1 + 8 + sid.length + 4) // sid.length = 16
					.add(Protocol.CONNECT_SUCCESS)
					.add(id)
					.add(sid)
					.add(server.getReconnectTimeoutSeconds())
					.build()
			);

			if (reconnect) {
				connection.write(receiveMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);
			}

			handleConnect();
		});
	}

	void handleConnectionBroken() {
		addTask(() -> {
			active = false;
			connection = null;
			int reconnectTimeoutSeconds = server.getReconnectTimeoutSeconds();
			if (reconnectTimeoutSeconds <= 0) {
				disconnect(Protocol.CLOSE);
			}
			else {
				this.reconnectTimeout = taskQueue.schedule(() -> {
					this.reconnectTimeout = null;
					disconnect(Protocol.CLOSE_RECONNECT_TIMEOUT_EXPIRED);
				}, reconnectTimeoutSeconds, TimeUnit.SECONDS);
			}
		});
	}

	void disconnect() {
		disconnect((byte)0, null);
	}

	void disconnect(byte reason) {
		disconnect(reason, null);
	}

	@SuppressWarnings("unchecked")
	void disconnect(byte reason, Runnable callback) {
		addTask(() -> {
			connected = false;
			cancelReconnectTimeout();

			if (disconnectHandlers != null) {
				for (Runnable handler : disconnectHandlers) {
					try {
						handler.run();
					}
					catch (Throwable e) {
						logger.warn("Error on handle disconnect", e);
					}
				}
			}

			try {
				handleDisconnect();
			}
			catch (Throwable e) {
				logger.warn("Error on handle disconnect", e);
			}

			server.removeClient(this);
			if (active) {
				if (reason == 0) {
					connection.close();
				}
				else {
					connection.close(reason);
				}
				connection = null;
				active = false;
			}

			if (callback != null) {
				callback.run();
			}
		});
	}

	private void sendMessage0(byte[] data) {
		sendMessageReady = false;
		sendMessageEven = !sendMessageEven;
		connection.write(new ByteArrayBuilder(1 + 4 + data.length)
			.add(sendMessageEven ? Protocol.MESSAGE_EVEN : Protocol.MESSAGE_ODD)
			.add(data.length)
			.add(data)
			.build());
	}

	private boolean cancelReconnectTimeout() {
		if (reconnectTimeout != null) {
			reconnectTimeout.cancel();
			reconnectTimeout = null;
			return true;
		}
		return false;
	}
}
