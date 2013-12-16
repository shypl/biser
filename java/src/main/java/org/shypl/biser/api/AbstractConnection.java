package org.shypl.biser.api;

public abstract class AbstractConnection<C extends ConnectionChannel>
{
	protected final C channel;
	protected final Logger            logger;
	private final Object closeLock = new Object();
	private volatile boolean closed;

	protected AbstractConnection(final C channel, final Logger logger)
	{
		this.channel = channel;
		this.logger = logger;
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
