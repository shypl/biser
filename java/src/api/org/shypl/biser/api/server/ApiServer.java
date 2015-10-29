package org.shypl.biser.api.server;

import org.apache.commons.codec.digest.DigestUtils;
import org.shypl.biser.api.ByteArrayBuilder;
import org.shypl.biser.api.Protocol;
import org.shypl.biser.api.ServerEntryAddress;
import org.shypl.common.concurrent.ScheduledTask;
import org.shypl.common.concurrent.TaskQueue;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiServer<C extends AbstractClient> {

	private static final int DEFAULT_RECONNECT_TIMEOUT_SECONDS = 10 * 60;

	private final Logger logger;
	private final ScheduledExecutorService executor;
	private final ApiGate<C>               gate;
	private final ServerEntryAddress       address;
	private final int                      reconnectTimeoutSeconds;
	private final TaskQueue                taskQueue;
	private final byte[]                   crossDomainPolicyResponse;
	private final ServerEntry              entry;

	private final AtomicInteger connectionCount         = new AtomicInteger();
	private final byte[]        clientConnectionSidSalt = new byte[8];

	private volatile boolean      running = true;
	private          Map<Long, C> clients = new HashMap<>();
	private ScheduledTask stopCheckTask;

	public ApiServer(ScheduledExecutorService executor, ServerEntryProvider entryProvider, ApiGate<C> gate, ServerEntryAddress address) {
		this(executor, entryProvider, gate, address, DEFAULT_RECONNECT_TIMEOUT_SECONDS);
	}

	public ApiServer(ScheduledExecutorService executor, ServerEntryProvider entryProvider, ApiGate<C> gate, ServerEntryAddress address,
		int reconnectTimeoutSeconds)
	{
		this(executor, entryProvider, gate, address,
			"<?xml version=\"1.0\"?>\n"
				+ "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">\n"
				+ "<cross-domain-policy>\n"
				+ "<site-control permitted-cross-domain-policies=\"all\" />\n"
				+ "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\"/>\n"
				+ "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>\n"
				+ "</cross-domain-policy>", reconnectTimeoutSeconds
		);
	}

	public ApiServer(ScheduledExecutorService executor, ServerEntryProvider entryProvider, ApiGate<C> gate, ServerEntryAddress address,
		String crossDomainPolicy)
	{
		this(executor, entryProvider, gate, address, crossDomainPolicy, DEFAULT_RECONNECT_TIMEOUT_SECONDS);
	}

	public ApiServer(ScheduledExecutorService executor, ServerEntryProvider entryProvider, ApiGate<C> gate, ServerEntryAddress address,
		String crossDomainPolicy, int reconnectTimeoutSeconds)
	{
		logger = new PrefixedLoggerProxy(LoggerFactory.getLogger(ApiServer.class), "<" + address + "> ");

		this.executor = executor;
		this.gate = gate;
		this.address = address;
		this.reconnectTimeoutSeconds = reconnectTimeoutSeconds;

		logger.info("Start...");

		gate.setServer(this);

		taskQueue = new TaskQueue(executor);

		new Random().nextBytes(clientConnectionSidSalt);

		byte[] crossDomainPolicyBytes = crossDomainPolicy.getBytes(StandardCharsets.UTF_8);
		crossDomainPolicyResponse = new byte[crossDomainPolicyBytes.length + 1];
		System.arraycopy(crossDomainPolicyBytes, 0, crossDomainPolicyResponse, 0, crossDomainPolicyBytes.length);
		crossDomainPolicyResponse[crossDomainPolicyBytes.length] = 0;

		entry = entryProvider.open(address, this::acceptConnection);

		logger.info("Started");
	}

	public void stop() {
		if (running) {
			taskQueue.add(() -> {
				if (running) {
					running = false;
					logger.info("Stop...");
					for (AbstractClient client : clients.values()) {
						client.disconnect(Protocol.CLOSE_SERVER_DOWN);
					}
					stopCheckTask = taskQueue.schedulePeriodic(this::doStop, 0, 1, TimeUnit.SECONDS);
				}
				else {
					logger.error("Is already stopped");
				}
			});
			synchronized (this) {
				try {
					wait();
				}
				catch (InterruptedException e) {
					logger.error("Stop is interrupted", e);
				}
			}
		}
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public ApiGate<C> getGate() {
		return gate;
	}

	public Logger getLogger() {
		return logger;
	}

	public void getClient(long clientId, ClientReceiver<C> receiver) {
		taskQueue.add(() -> {
			C client = clients.get(clientId);
			if (client == null) {
				receiver.receiveNotConnectedClient(clientId);
			}
			else {
				receiver.receiveConnectedClient(client);
			}
		});
	}

	int getReconnectTimeoutSeconds() {
		return reconnectTimeoutSeconds;
	}

	void sendMessageForAllClients(byte[] message) {
		taskQueue.add(() -> {
			for (AbstractClient client : clients.values()) {
				client.sendMessage(message);
			}
		});
	}

	byte[] getClientConnectionSid(AbstractClient client) {
		return DigestUtils.md5(
			new ByteArrayBuilder(8 + 8 + clientConnectionSidSalt.length)
				.add(client.getId())
				.add(client.getConnectionId())
				.add(clientConnectionSidSalt)
				.build()
		);
	}

	byte[] getCrossDomainPolicyResponse() {
		return crossDomainPolicyResponse;
	}

	ConnectionChannelHandler acceptConnection(ConnectionChannel channel) {
		if (running) {
			int i = connectionCount.incrementAndGet();
			logger.trace("Accept connection {} (connections: {})", channel.getRemoteAddress(), i);

			Connection connection = new Connection(this, channel);
			connection.setStrategy(new ConnectionStrategyReception());
			return connection;
		}
		else {
			channel.write(Protocol.CLOSE_SERVER_DOWN);
			return null;
		}
	}

	void releaseConnection(Connection connection) {
		int i = connectionCount.decrementAndGet();
		logger.trace("Release connection {} (connections: {})", connection.getRemoteAddress(), i);
	}

	void connectClient(C client, Connection connection) {
		taskQueue.add(() -> {
			if (connection.isOpened()) {
				if (running) {
					C oldClient = clients.get(client.getId());

					if (oldClient == null) {
						clients.put(client.getId(), client);

						logger.trace("Connect client id={} (clients: {})", client.getId(), clients.size());

						client.secConnection(connection);
					}
					else {
						logger.trace("Connect concurrent client id={}", client.getId());
						oldClient.disconnect(Protocol.CLOSE_CONCURRENT_CONNECT, () -> connectClient(client, connection));
					}
				}
				else {
					connection.close(Protocol.CLOSE_SERVER_DOWN);
				}
			}
		});
	}

	void reconnectClient(long clientId, byte[] sid, Connection connection) {
		taskQueue.add(() -> {
			if (connection.isOpened()) {
				if (running) {
					AbstractClient client = clients.get(clientId);

					if (client == null || !Arrays.equals(getClientConnectionSid(client), sid)) {
						connection.close(Protocol.CLOSE_RECONNECT_REJECT);
					}
					else {
						logger.trace("Reconnect client id={} (clients: {})", client.getId(), clients.size());
						client.secConnection(connection);
					}
				}
				else {
					connection.close(Protocol.CLOSE_SERVER_DOWN);
				}
			}
		});
	}

	void removeClient(C client) {
		taskQueue.add(() -> {
			C currentClient = clients.get(client.getId());
			if (currentClient == client) {
				clients.remove(client.getId());
				logger.trace("Remove client id={} (clients: {})", client.getId(), clients.size());
			}
			else {
				logger.warn("Lose client id={}", client.getId(), clients.size());
			}
			gate.handleDisconnectClient(client);
		});
	}

	private void doStop() {
		int cc = connectionCount.get();
		logger.info("Stop check: clients: {}, connections: {}", clients.size(), cc);

		if (clients.isEmpty() && cc == 0) {
			stopCheckTask.cancel();
			stopCheckTask = null;

			entry.close();
			logger.info("Stopped", address);

			synchronized (this) {
				notifyAll();
			}
		}
	}
}
