package org.shypl.biser.csi.server;

public interface ClientReceiver<C extends AbstractClient> {
	default void receiveNotConnectedClient(long clientId) {
	}

	void receiveConnectedClient(C client);
}
