package org.shypl.biser.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

public class ClientController<C extends AbstractClient>
{
	final         ClientFactory<C>         clientFactory;
	final         AbstractServiceRouter<C> router;
	final         ScheduledExecutorService scheduler;
	private final byte[]                   policyResponse;
	private final int                      timeout;

	public ClientController(final int timeout, final ClientFactory<C> clientFactory,
		final AbstractServiceRouter<C> router, final ScheduledExecutorService scheduler)
	{
		this(timeout, clientFactory, router, scheduler, "<?xml version=\"1.0\"?>\n"
			+ "<cross-domain-policy>"
			+ "<allow-access-from domain=\"*\" to-ports=\"*\"/>"
			+ "</cross-domain-policy>");
	}

	public ClientController(final int timeout, final ClientFactory<C> clientFactory,
		final AbstractServiceRouter<C> router, final ScheduledExecutorService scheduler, final Path policyFile)
		throws IOException
	{
		this(timeout, clientFactory, router, scheduler, Files.readAllBytes(policyFile));
	}

	public ClientController(final int timeout, final ClientFactory<C> clientFactory,
		final AbstractServiceRouter<C> router, final ScheduledExecutorService scheduler, final String policyResponse)
	{
		this(timeout, clientFactory, router, scheduler, policyResponse.getBytes());
	}

	public ClientController(final int timeout, final ClientFactory<C> clientFactory,
		final AbstractServiceRouter<C> router, final ScheduledExecutorService scheduler, final byte[] policyResponse)
	{
		if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout must be greater than zero");
		}

		this.timeout = timeout;
		this.clientFactory = clientFactory;
		this.router = router;
		this.scheduler = scheduler;

		if (policyResponse[policyResponse.length - 1] != 0) {
			this.policyResponse = Arrays.copyOf(policyResponse, policyResponse.length + 1);
		}
		else {
			this.policyResponse = policyResponse;
		}
	}

	public int getTimeout()
	{
		return timeout;
	}

	byte[] getPolicyResponse()
	{
		return policyResponse;
	}
}
