package org.shypl.biser.server;

public interface ClientFactory<C extends AbstractClient>
{
	C createClient(final ClientConnection<C> connection, final ClientController<C> controller);
}
