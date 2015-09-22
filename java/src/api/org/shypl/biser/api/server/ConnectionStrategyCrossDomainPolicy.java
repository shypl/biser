package org.shypl.biser.api.server;

import org.apache.commons.codec.binary.Hex;
import org.shypl.biser.api.ProtocolException;

import java.nio.charset.StandardCharsets;

class ConnectionStrategyCrossDomainPolicy extends ConnectionStrategy {
	private final static byte[] PATTERN;

	static {
		byte[] bytes = "policy-file-request/>".getBytes(StandardCharsets.UTF_8);
		PATTERN = new byte[bytes.length + 1];
		PATTERN[bytes.length] = 0;
	}

	private int position = 0;

	@Override
	public void handleData() throws ProtocolException {
		do {
			byte b = connection.readSync();
			if (b == PATTERN[position]) {
				if (++position == PATTERN.length) {
					connection.getLogger().trace("CrossDomainPolicy: Valid request received, sent response and close");
					connection.writeSync(connection.getServer().getCrossDomainPolicyResponse());
					connection.closeSync();
					break;
				}
			}
			else {
				byte[] request = new byte[position];
				System.arraycopy(PATTERN, 0, request, 0, position + 1);
				request[position + 1] = b;

				throw new ProtocolException(
					"CrossDomainPolicy: Invalid request (" + Hex.encodeHexString(request) + ":" + new String(request, StandardCharsets.UTF_8) + ")");
			}
		}
		while (connection.isReadableSync());
	}

	@Override
	public void handleClose(boolean broken) {}
}
