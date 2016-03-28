package org.shypl.biser.csi.server;

import org.shypl.biser.csi.Address;

public interface ChannelGate {
	void open(Address address, ChannelAcceptor acceptor);

	void close();
}
