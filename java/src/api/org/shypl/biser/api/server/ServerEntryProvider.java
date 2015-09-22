package org.shypl.biser.api.server;

import org.shypl.biser.api.ServerEntryAddress;

public interface ServerEntryProvider {
	ServerEntry open(ServerEntryAddress address, ConnectionChannelAcceptor connectionAcceptor);
}
