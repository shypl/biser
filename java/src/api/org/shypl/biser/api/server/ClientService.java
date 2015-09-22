package org.shypl.biser.api.server;

import org.shypl.biser.io.ByteArrayWriter;

public abstract class ClientService {
	private final int            id;
	private final AbstractClient client;

	public ClientService(int id, AbstractClient client) {
		this.id = id;
		this.client = client;
	}

	protected final void _send(ByteArrayWriter writer) {
		client.sendMessage(writer.toByteArray());
	}

	protected final ByteArrayWriter _createWriter(int actionId) {
		ByteArrayWriter writer = new ByteArrayWriter();
		writer.writeInt(0);
		writer.writeInt(id);
		writer.writeInt(actionId);
		return writer;
	}

	protected final void _log(String message, Object... args) {
		client.getLogger().trace(message, args);
	}
}
