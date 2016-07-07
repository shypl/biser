package org.shypl.biser.csi.server;

public interface ClientFactory<C extends AbstractClient> {
	C factoryClient(String authorizationKey);
}
