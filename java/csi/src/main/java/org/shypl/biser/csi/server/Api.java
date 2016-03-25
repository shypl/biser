package org.shypl.biser.csi.server;

import org.shypl.biser.csi.CommunicationLoggingUtils;
import org.shypl.biser.io.ByteArrayInputData;
import org.shypl.biser.io.ByteArrayOutputData;
import org.shypl.biser.io.DataReader;
import org.shypl.biser.io.DataWriter;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Api<C extends Client> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

	private static final ThreadLocal<ByteArrayInputData>  threadLocalInputData  = new ThreadLocal<ByteArrayInputData>() {
		@Override
		protected ByteArrayInputData initialValue() {
			return new ByteArrayInputData();
		}
	};
	private static final ThreadLocal<ByteArrayOutputData> threadLocalOutputData = new ThreadLocal<ByteArrayOutputData>() {
		@Override
		protected ByteArrayOutputData initialValue() {
			return new ByteArrayOutputData();
		}
	};

	private final Map<Long, C> clients = new ConcurrentHashMap<>();
	private final String name;
	private final Logger logger;

	protected Api(String name) {
		this.name = name;
		logger = new PrefixedLoggerProxy(LOGGER, '[' + name + "] ");
	}

	public final String getName() {
		return name;
	}

	public final int countClients() {
		return clients.size();
	}

	public final Collection<C> getAllClients() {
		return clients.values();
	}

	public final Collection<C> getClients(Collection<Long> ids) {
		LinkedList<C> result = new LinkedList<>();
		for (Long id : ids) {
			C client = clients.get(id);
			result.add(client);
		}
		return result;
	}

	public final C getClient(long id) {
		return clients.get(id);
	}

	public final void sendMessage(GlobalMessage message) {
		sendMessage(message, clients.values());
	}

	public final void sendMessage(GlobalMessage message, Collection<? extends Client> clients) {
		message.send(clients, logger);
	}

	protected abstract C authorizeClient(String key);

	protected abstract void callService(C client, int serviceId, int methodId, DataReader reader, DataWriter writer) throws Throwable;

	protected final void logCall(Client client, String serviceName, String methodName) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName);
	}

	protected final void logCall(Client client, String serviceName, String methodName, Object arg) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName, arg);
	}

	protected final void logCall(Client client, String serviceName, String methodName, Object arg1, Object arg2) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName, arg1, arg2);
	}

	protected final void logCall(Client client, String serviceName, String methodName, Object... args) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName, args);
	}

	protected final void logResponse(Client client, String serviceName, String methodName, Object result) {
		CommunicationLoggingUtils.logServerResponse(client.getLogger(), serviceName, methodName, result);
	}


	Client makeClient(String key) {
		Client client = authorizeClient(key);
		if (client != null) {
			client.init(name);
		}
		return client;
	}

	@SuppressWarnings("unchecked")
	void addClient(Client client) {
		clients.put(client.getId(), (C)client);
	}

	void removeClient(long id) {
		clients.remove(id);
	}

	@SuppressWarnings("unchecked")
	void processIncomingMessage(Client client, byte[] message) throws Throwable {
		ByteArrayInputData inputData = threadLocalInputData.get();
		ByteArrayOutputData outputData = threadLocalOutputData.get();

		inputData.reset(message);
		outputData.clear();

		DataReader reader = inputData.getReader();
		DataWriter writer = outputData.getWriter();

		callService((C)client, reader.readInt(), reader.readInt(), reader, writer);

		if (outputData.isNotEmpty()) {
			client.sendMessage(outputData.getArray());
		}
	}
}
