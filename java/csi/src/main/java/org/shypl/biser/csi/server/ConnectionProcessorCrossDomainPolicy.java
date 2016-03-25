package org.shypl.biser.csi.server;

import org.apache.commons.codec.binary.Hex;
import org.shypl.biser.csi.ProtocolException;

import java.nio.charset.StandardCharsets;

class ConnectionProcessorCrossDomainPolicy extends ConnectionProcessor {
	private static final byte[] PATTERN;

	static {
		byte[] bytes = "policy-file-request/>".getBytes(StandardCharsets.UTF_8);
		PATTERN = new byte[bytes.length + 1];
		System.arraycopy(bytes, 0, PATTERN, 0, bytes.length);
		PATTERN[bytes.length] = 0;
	}

	private int position = 0;

	@Override
	public void processData() throws ProtocolException {
		do {
			byte b = connection.read();
			if (b == PATTERN[position]) {
				if (++position == PATTERN.length) {
					connection.getLogger().debug("CrossDomainPolicy: Valid request received, sent response and close");
					connection.syncSend(connection.getServer().getSettings().getCrossDomainPolicyResponse());
					connection.syncClose();
					break;
				}
			}
			else {
				byte[] request = new byte[position + 1];
				System.arraycopy(PATTERN, 0, request, 0, position);
				request[position] = b;

				throw new ProtocolException(String.format("CrossDomainPolicy: Invalid request %s (%s)",
					new String(request, StandardCharsets.UTF_8), Hex.encodeHexString(request)));
			}
		}
		while (connection.isReadable());
	}
}
