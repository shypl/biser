package org.shypl.biser.server;

import org.shypl.biser.OutputBuffer;

public abstract class ServerApiResult
{
	protected final OutputBuffer     message;
	private final   ClientConnection connection;
	private         boolean          sent;

	protected ServerApiResult(final int id, final ClientConnection connection)
	{
		this.connection = connection;
		message = new OutputBuffer();
		message.writeInt(id);
	}

	protected final void send()
	{
		if (sent) {
			throw new IllegalStateException();
		}
		sent = true;
		connection.sendMessage(message.bytes());
	}
}
