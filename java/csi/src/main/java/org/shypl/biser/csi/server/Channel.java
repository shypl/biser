package org.shypl.biser.csi.server;

import java.net.SocketAddress;

public interface Channel {
	SocketAddress getRemoteAddress();

	void write(byte[] bytes);

	void write(byte b);

	void close();
}
