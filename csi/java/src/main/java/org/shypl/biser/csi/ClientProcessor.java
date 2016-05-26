package org.shypl.biser.csi;

import org.shypl.biser.csi.server.AbstractClient;

public interface ClientProcessor<C extends AbstractClient> {
	void processConnectedClient(C client);

	default void processNotConnectedClient(long clientId) {
	}
}
