package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ClientProcessor;
import org.shypl.biser.csi.CommunicationLoggingUtils;
import org.shypl.biser.io.ByteArrayInputData;
import org.shypl.biser.io.ByteArrayOutputData;
import org.shypl.biser.io.DataReader;
import org.shypl.biser.io.DataWriter;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.shypl.common.util.Cancelable;
import org.shypl.common.util.Observers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractApi<C extends AbstractClient> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApi.class);

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
	private final Function<String, C> clientFactory;
	private final Logger logger;

	private final Observers<Consumer<C>> clientConnectObservers    = new Observers<>();
	private final Observers<Consumer<C>> clientDisconnectObservers = new Observers<>();

	protected AbstractApi(String name, Function<String, C> clientFactory) {
		this.name = name;
		this.clientFactory = clientFactory;
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

	public final void processClient(long id, ClientProcessor<C> receiver) {
		C client = clients.get(id);

		if (client != null && client.isConnected()) {
			client.getWorker().addTask(() -> {
				if (client.isConnected()) {
					receiver.processConnectedClient(client);
				}
				else {
					receiver.processNotConnectedClient(id);
				}
			});
		}
		else {
			receiver.processNotConnectedClient(id);
		}

	}

	public final void sendMessage(GlobalMessage message) {
		sendMessage(message, clients.values());
	}

	public final void sendMessage(GlobalMessage message, Collection<? extends AbstractClient> clients) {
		message.send(clients, logger);
	}

	public final Cancelable addClientConnectObserver(Consumer<C> observer) {
		return clientConnectObservers.add(observer);
	}

	public final void removeClientConnectObserver(Consumer<C> observer) {
		clientConnectObservers.remove(observer);
	}

	public final Cancelable addClientDisconnectObserver(Consumer<C> observer) {
		return clientDisconnectObservers.add(observer);
	}

	public final void removeClientDisconnectObserver(Consumer<C> observer) {
		clientDisconnectObservers.remove(observer);
	}

	protected abstract void callService(C client, int serviceId, int methodId, DataReader reader, DataWriter writer) throws Throwable;

	protected final void logCall(AbstractClient client, String serviceName, String methodName) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName);
	}

	protected final void logCall(AbstractClient client, String serviceName, String methodName, Object arg) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName, arg);
	}

	protected final void logCall(AbstractClient client, String serviceName, String methodName, Object arg1, Object arg2) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName, arg1, arg2);
	}

	protected final void logCall(AbstractClient client, String serviceName, String methodName, Object... args) {
		CommunicationLoggingUtils.logClientCall(client.getLogger(), serviceName, methodName, args);
	}

	protected final void logResponse(AbstractClient client, String serviceName, String methodName, Object result) {
		CommunicationLoggingUtils.logServerResponse(client.getLogger(), serviceName, methodName, result);
	}

	AbstractClient makeClient(String key) {
		AbstractClient client = clientFactory.apply(key);
		if (client != null) {
			client.init(name);
		}
		return client;
	}

	void addClient(AbstractClient client) {
		@SuppressWarnings("unchecked")
		C c = (C)client;
		clients.put(client.getId(), c);
		clientConnectObservers.inform(observer -> observer.accept(c));
	}

	void removeClient(AbstractClient client) {
		@SuppressWarnings("unchecked")
		C c = (C)client;
		clients.remove(client.getId());
		clientDisconnectObservers.inform(observer -> observer.accept(c));
	}

	@SuppressWarnings("unchecked")
	void processIncomingMessage(AbstractClient client, byte[] message) throws Throwable {
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
