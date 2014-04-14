package org.shypl.biser.server;

import org.shypl.biser.InputBuffer;
import org.shypl.biser.OutputBuffer;

public abstract class AbstractServiceRouter<C extends AbstractClient>
{
	void receiveMessage(final C client, final byte[] message) throws Exception
	{
		try {
			final InputBuffer buffer = new InputBuffer(message);
			final OutputBuffer response = route(client, buffer.readInt(), buffer.readInt(), buffer);
			if (response != null) {
				client.connection.send(response.bytes());
			}
		}
		catch (Exception e) {
			client.disconnect();
			throw e;
		}
	}

	protected abstract OutputBuffer route(final C client, final int service, final int method, final InputBuffer buffer)
		throws Exception;
}
