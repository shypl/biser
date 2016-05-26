package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ConnectionCloseReason;
import org.shypl.biser.csi.ProtocolException;

import java.nio.charset.StandardCharsets;

class ConnectionProcessorAuthorization extends ConnectionProcessor {
	private byte[] key;
	private int    index;

	@Override
	public void processData() throws ProtocolException {

		if (index == -1) { // wait authorization
			throw new ProtocolException("Authorization: Received data while waiting for authorization");
		}

		if (key == null) { // key size not received
			connection.getLogger().debug("Authorization: Read authorization key");
			int size = 0xFF & connection.read();
			if (size <= 0) {
				throw new ProtocolException("Authorization: Bad authorization key size " + size);
			}
			key = new byte[size];
		}

		// read key
		index += connection.read(key, index);

		if (index == key.length) {
			index = -1;
			authorize(new String(this.key, StandardCharsets.UTF_8));
		}
	}

	private void authorize(String key) {
		connection.getLogger().debug("Authorization: Authorize client by key {}", key);

		Server server = connection.getServer();
		AbstractClient client = server.getApi().makeClient(key);

		if (client == null) {
			connection.getLogger().warn("Authorization: Reject", key);
			connection.syncClose(ConnectionCloseReason.AUTHORIZATION_REJECT);
		}
		else {
			connection.getLogger().debug("Authorization: Success (clientId: {})", client.getId());
			server.connectClient(client, connection);
		}
	}

}
