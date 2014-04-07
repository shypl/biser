package org.shypl.biser.server;

import org.shypl.biser.OutputBuffer;

public abstract class ClientApiService
{
	private final int              id;
	private final ClientConnection connection;

	protected ClientApiService(final int id, final ClientConnection connection)
	{
		this.id = id;
		this.connection = connection;
	}

	protected final OutputBuffer _createMessage(final int method)
	{
		final OutputBuffer buffer = new OutputBuffer();
		buffer.writeInt(0);
		buffer.writeInt(id);
		buffer.writeInt(method);
		return buffer;
	}

	protected final void _sendMessage(final OutputBuffer message)
	{
		connection.send(message.bytes());
	}
}
