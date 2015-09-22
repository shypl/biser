package org.shypl.biser.api.server;

import org.apache.commons.codec.binary.Hex;
import org.shypl.biser.api.ProtocolException;
import org.shypl.common.util.ByteUtils;

import java.util.Arrays;

class ConnectionStrategyReconnect extends ConnectionStrategy {

	private byte[] buffer = new byte[8 + 16];
	private int    index  = 0;

	@Override
	public void handleData() throws ProtocolException {
		do {
			if (index == buffer.length) {
				throw new ProtocolException("Reconnect: Received data while waiting for reconnection");
			}

			index += connection.readSync(buffer, index);
			if (index == buffer.length) {
				long clientId = ByteUtils.readLong(buffer, 0);;
				buffer = Arrays.copyOfRange(buffer, 8, buffer.length);
				if (connection.getLogger().isTraceEnabled()) {
					connection.getLogger().trace("Reconnect: Try to establish connection (clientId: {}, sid: {})", clientId, Hex.encodeHexString(buffer));
				}
				connection.getServer().reconnectClient(clientId, buffer, connection);
			}
		}
		while (connection.isReadableSync());
	}

	@Override
	public void handleClose(boolean broken) {}
}
