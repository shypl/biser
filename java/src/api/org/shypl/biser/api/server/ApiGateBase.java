package org.shypl.biser.api.server;

import org.shypl.biser.api.Protocol;
import org.shypl.biser.io.ByteArrayReader;
import org.shypl.biser.io.ByteArrayWriter;
import org.slf4j.LoggerFactory;

public abstract class ApiGateBase<C extends AbstractClient> {

	private final ThreadLocal<ByteArrayWriter> outputStream = new ThreadLocal<ByteArrayWriter>() {
		@Override
		protected ByteArrayWriter initialValue() {
			return new ByteArrayWriter();
		}
	};
	private ApiServer<C> server;

	public final void sendGlobalMessage(GlobalMessage message) {
		message.send(server);
	}

	public final void getClient(long clientId, ClientReceiver<C> receiver) {
		server.getClient(clientId, receiver);
	}

	void setServer(ApiServer<C> server) {
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
			if (client.isActive()) {
				client.getConnection().getLogger().error("Error on process client message", e);
				client.getConnection().close(Protocol.CLOSE_SERVER_ERROR);
			}
			else {
				LoggerFactory.getLogger(ApiGateBase.class).error("Error on process client message", e);
			}
		}
	}

	protected void handleDisconnectClient(C client) {}

	protected abstract void execute(C client, int serviceId, int actionId, ByteArrayReader reader, ByteArrayWriter writer) throws Exception;

	protected abstract C connectClient(String key) throws Exception;
}
