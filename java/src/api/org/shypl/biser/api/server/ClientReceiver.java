package org.shypl.biser.api.server;

public interface ClientReceiver<C extends AbstractClient> {
	void receiveConnectedClient(C client);

	void receiveNotConnectedClient(long clientId);
}
