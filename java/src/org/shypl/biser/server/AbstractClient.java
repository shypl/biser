package org.shypl.biser.server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClient
{
	protected final ClientConnection   connection;
	private         ScheduledFuture<?> authorizeTimeout;

	protected AbstractClient(final ClientConnection connection, final ClientController controller,
		final boolean authorized)
	{
		this.connection = connection;

		if (!authorized) {
			authorizeTimeout =
				controller.scheduler.schedule(new Runnable()
				{
					@Override
					public void run()
					{
						disconnect();
					}
				}, controller.getTimeout(), TimeUnit.SECONDS);
		}
	}

	public void authorize()
	{
		cancelAuthorizeTimeout();
	}

	public final void disconnect()
	{
		connection.close();
	}

	public ClientConnection getConnection()
	{
		return connection;
	}

	protected void handleDisconnect()
	{
		cancelAuthorizeTimeout();
	}

	private void cancelAuthorizeTimeout()
	{
		if (authorizeTimeout != null) {
			authorizeTimeout.cancel(false);
			authorizeTimeout = null;
		}
	}
}
