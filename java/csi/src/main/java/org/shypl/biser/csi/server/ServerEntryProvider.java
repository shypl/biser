package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ServerEntryAddress;

public interface ServerEntryProvider {
	ServerEntry open(ServerEntryAddress address, ConnectionChannelAcceptor connectionAcceptor);
}
