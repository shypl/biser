package org.shypl.biser.api.server;

import org.shypl.biser.api.Protocol;
import org.shypl.biser.api.ProtocolException;
import org.shypl.biser.io.ByteArrayReader;
import org.shypl.biser.io.ByteArrayWriter;
import org.slf4j.LoggerFactory;

public abstract class ApiGate<C extends AbstractClient> {

	private final ThreadLocal<ByteArrayWriter> outputStream  = new ThreadLocal<ByteArrayWriter>() {
		@Override
		protected ByteArrayWriter initialValue() {
			return new ByteArrayWriter();
		}
	};
	private final ThreadLocal<C>               currentClient = new ThreadLocal<>();
	private ApiServer server;

	public void sendGlobalMessage(GlobalMessage message) {
		message.send(server);
	}

	void setServer(ApiServer server) {
		this.server = server;
	}

	void processMessage(AbstractClient client, byte[] data) {
		try {
			setCurrentClient(client);

			ByteArrayReader reader = new ByteArrayReader(data);
			ByteArrayWriter writer = this.outputStream.get();

			getService(reader.readInt())._executeAction(reader.readInt(), reader, writer);

			if (writer.isNotEmpty()) {
				client.sendMessage(writer.toByteArray());
				writer.clear();
			}

			currentClient.set(null);
		}
		catch (Exception e) {
			if (client.hasConnection()) {
				client.getConnection().getLogger().error("Error on process client message", e);
				client.getConnection().close(Protocol.CLOSE_SERVER_ERROR);
			}
			else {
				LoggerFactory.getLogger(ApiGate.class).error("Error on process client message", e);
			}
		}
	}

	C getCurrentClient() {
		return currentClient.get();
	}

	protected abstract Service<C> getService(int id) throws ProtocolException;

	protected abstract C connectClient(String key);

	@SuppressWarnings("unchecked")
	private void setCurrentClient(AbstractClient client) {
		currentClient.set((C)client);
	}

	protected final void registerService(Service<C> service) {
		service.setGate(this);
	}
}
