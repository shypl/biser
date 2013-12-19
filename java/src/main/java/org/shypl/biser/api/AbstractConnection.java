package org.shypl.biser.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractConnection<C extends ConnectionChannel>
{
	protected static final Logger logger = LogManager.getLogger(AbstractConnection.class);
	protected final C channel;
	private final Object closeLock = new Object();
	private volatile boolean closed;

	protected AbstractConnection(final C channel)
	{
		this.channel = channel;
	}

	public final void close()
	{
		if (closed) {
			return;
		}

		synchronized (closeLock) {
			if (closed) {
				return;
			}
			closed = true;
		}

		channel.close();

		handleClose();
	}

	protected abstract void handleClose();
}
