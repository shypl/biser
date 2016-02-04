package org.shypl.biser.csi.server;

import java.net.SocketAddress;

public interface ConnectionChannel {
	SocketAddress getRemoteAddress();

	void write(byte[] bytes);

	void write(byte b);

	void close();
}
