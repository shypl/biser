package org.shypl.biser.api.server;

public interface ConnectionChannelAcceptor {
	ConnectionChannelHandler accept(ConnectionChannel channel);
}
