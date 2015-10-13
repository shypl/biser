package org.shypl.biser.api.server;

public interface ClientReceiver<C extends AbstractClient> {
	default void receiveNotConnectedClient(long clientId) {}

	void receiveConnectedClient(C client);
}
