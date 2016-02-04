package org.shypl.biser.csi.server;

public interface ConnectionChannelAcceptor {
	ConnectionChannelHandler accept(ConnectionChannel channel);
}
