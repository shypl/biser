package org.shypl.biser.csi.server;

public interface ChannelHandler {
	void handleChannelData(byte[] bytes);

	void handleChannelClose();
}
