package org.shypl.biser.api.server;

import org.shypl.biser.api.Protocol;
import org.shypl.biser.io.ByteArrayReader;
import org.shypl.biser.io.ByteArrayWriter;
import org.slf4j.LoggerFactory;

public abstract class ApiGate<C extends AbstractClient> {

	private final ThreadLocal<ByteArrayWriter> outputStream = new ThreadLocal<ByteArrayWriter>() {
		@Override
		protected ByteArrayWriter initialValue() {
			return new ByteArrayWriter();
		}
	};
	private ApiServer server;

	public void sendGlobalMessage(GlobalMessage message) {
		message.send(server);
	}

	public void getClient(long clientId, ClientReceiver receiver) {
		server.getClient(clientId, receiver);
	}

	void setServer(ApiServer server) {
		this.server = server;
	}

	void processMessage(C client, byte[] data) {
		try {

			ByteArrayReader reader = new ByteArrayReader(data);
			ByteArrayWriter writer = this.outputStream.get();

			execute(client, reader.readInt(), reader.readInt(), reader, writer);

			if (writer.isNotEmpty()) {
				client.sendMessage(writer.toByteArray());
				writer.clear();
			}
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

	protected abstract void execute(C client, int serviceId, int actionId, ByteArrayReader reader, ByteArrayWriter writer) throws Exception;

	protected abstract C connectClient(String key);
}
