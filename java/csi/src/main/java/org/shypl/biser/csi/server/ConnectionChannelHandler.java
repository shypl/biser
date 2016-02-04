package org.shypl.biser.csi.server;

public interface ConnectionChannelHandler {
	void handleData(byte[] data);

	void handleClose();
}
