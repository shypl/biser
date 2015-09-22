package org.shypl.biser.api.server;

import org.shypl.biser.api.ByteArrayBuilder;
import org.shypl.biser.api.Protocol;
import org.shypl.common.concurrent.ScheduledTask;
import org.shypl.common.concurrent.TaskQueue;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

	private final long                id;
	private final PrefixedLoggerProxy logger;
	private       Connection          connection;
	private       long              connectionId;
	private       ApiServer           server;
	private       TaskQueue           taskQueue;

	private volatile boolean connected = true;
	private ScheduledTask reconnectTimeout;
	private boolean       receiveMessageEven;
	private boolean       sendMessageEven;
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

	public Logger getLogger() {
		return logger;
	}

	boolean hasConnection() {
		return connection != null;
	}

	Connection getConnection() {
		return connection;
	}

	void setServer(ApiServer server) {
		if (this.server != null) {
			throw new IllegalStateException();
		}
		this.server = server;
		taskQueue = new TaskQueue(server.getExecutor());
	}

	void receiveMessage(boolean even, byte[] data) {
		taskQueue.add(() -> {
			if (connected) {
				receiveMessageEven = even;
				connection.write(receiveMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);
				server.getGate().processMessage(this, data);
			}
		});
	}

	void sendMessage(byte[] data) {
		taskQueue.add(() -> {
			messages.addLast(data);
			if (connected && sendMessageReady) {
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
		taskQueue.add(() -> {
			if (connected) {
				this.connection = connection;
				this.connectionId = connection.getId();
				boolean r = cancelReconnectTimeout();
				connection.setStrategy(new ConnectionStrategyMessaging(this));
				if (r) {
					connection.write(receiveMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);
				}
			}
		});
	}

	void handleConnectionBroken() {
		taskQueue.add(() -> {
			if (connected) {
				connection = null;
				reconnectTimeout = taskQueue.schedule(() -> {
					reconnectTimeout = null;
					disconnect(Protocol.CLOSE_RECONNECT_TIMEOUT_EXPIRED);
				}, 10, TimeUnit.MINUTES);
			}
		});
	}

	void disconnect(byte reason) {
		disconnect(reason, null);
	}

	void disconnect(byte reason, Runnable callback) {
		taskQueue.add(() -> {
			if (connected) {
				connected = false;
				cancelReconnectTimeout();
				handleDisconnect();

				server.removeClient(this);
				if (hasConnection()) {
					connection.close(reason);
					connection = null;
				}
				if (callback != null) {
					callback.run();
				}
			}
			else {
				server.removeClient(this);
				if (callback != null) {
					callback.run();
				}
			}
		});
	}

	protected void handleDisconnect() {}

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