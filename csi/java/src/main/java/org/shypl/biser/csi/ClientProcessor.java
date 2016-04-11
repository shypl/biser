package org.shypl.biser.csi;

import org.shypl.biser.csi.server.Client;

public interface ClientProcessor<C extends Client> {
	void processConnectedClient(C client);

	default void processNotConnectedClient(long clientId) {
	}
}
