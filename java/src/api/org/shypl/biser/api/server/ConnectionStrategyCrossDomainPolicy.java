package org.shypl.biser.api.server;

import org.apache.commons.codec.binary.Hex;
import org.shypl.biser.api.ProtocolException;

import java.nio.charset.StandardCharsets;

class ConnectionStrategyCrossDomainPolicy extends ConnectionStrategy {
	private final static byte[] PATTERN;

	static {
		byte[] bytes = "policy-file-request/>".getBytes(StandardCharsets.UTF_8);
		PATTERN = new byte[bytes.length + 1];
		System.arraycopy(bytes, 0, PATTERN, 0, bytes.length);
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
				byte[] request = new byte[position + 1];
				System.arraycopy(PATTERN, 0, request, 0, position);
				request[position] = b;

				throw new ProtocolException(
					"CrossDomainPolicy: Invalid request (" + new String(request, StandardCharsets.UTF_8) + " : " + Hex.encodeHexString(request) + ")");
			}
		}
		while (connection.isReadableSync());
	}

	@Override
	public void handleClose(boolean broken) {}
}
