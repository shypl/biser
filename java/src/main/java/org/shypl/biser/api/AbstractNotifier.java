package org.shypl.biser.api;

import org.shypl.biser.OutputBuffer;

public abstract class AbstractNotifier
{
	protected final int                _id;
	private final   AbstractConnection connection;

	protected AbstractNotifier(final int id, final AbstractConnection connection)
	{
		_id = id;
		this.connection = connection;
	}

	protected final void _send(final OutputBuffer buffer)
	{
		connection.channel.sendMessage(buffer);
	}

	protected final void _debug(final String msg, final Object... args)
	{
		connection.logger.debug(msg, args);
	}
}
