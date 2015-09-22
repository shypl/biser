package org.shypl.biser.api.server;

import java.net.SocketAddress;

public interface ConnectionChannel {
	SocketAddress getRemoteAddress();

	void write(byte[] bytes);

	void write(byte b);

	void close();
}
