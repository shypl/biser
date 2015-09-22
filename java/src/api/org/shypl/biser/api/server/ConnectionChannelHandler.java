package org.shypl.biser.api.server;

public interface ConnectionChannelHandler {
	void handleData(byte[] data);

	void handleClose();
}
