package org.shypl.biser.api;

import org.shypl.biser.InputBuffer;
import org.shypl.biser.OutputBuffer;

public abstract class AbstractController<C extends AbstractConnection>
{
	protected final Logger logger;

	protected AbstractController(final Logger logger)
	{
		this.logger = logger;
	}

	public final void receiveMessage(final C connection, final InputBuffer message) throws Exception
	{
		final OutputBuffer response = route(connection, message.readInt(), message.readInt(), message);
		if (response != null) {
			connection.channel.sendMessage(response);
		}
	}

	protected abstract OutputBuffer route(final C connection, final int service, final int method,
		final InputBuffer input)
		throws Exception;
}
