package org.shypl.biser.api.server;

import org.shypl.biser.api.Protocol;
import org.shypl.biser.api.ProtocolException;
import org.shypl.common.concurrent.ScheduledTask;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

class ConnectionStrategyConnect extends ConnectionStrategy {
	private byte[]        key;
	private int           index;
	private ScheduledTask timeout;

	@Override
	public void handleData() throws ProtocolException {
		if (index == -1) { // wait authorization
			throw new ProtocolException("Connect: Received data while waiting for authorization");
		}

		if (key == null) { // key size not received
			connection.getLogger().trace("Connect: Read authorization key");
			int size = 0xFF & connection.readSync();
			if (size <= 0) {
				throw new ProtocolException("Connect: Bad authorization key size (" + size + ")");
			}
			key = new byte[size];
		}

		// read key
		index += connection.readSync(key, index);

		if (index == key.length) {
			index = -1;
			timeout.cancel();
			authorize(new String(this.key, StandardCharsets.UTF_8));
		}
	}

	@Override
	public void handleClose(boolean broken) {
		timeout.cancel();
	}

	@Override
	void setConnection(Connection connection) {
		super.setConnection(connection);
		timeout = connection.getTaskQueue().schedule(() -> {
			this.connection.getLogger().warn("Connect: Authorization timeout expired");
			this.connection.closeSync(Protocol.CLOSE_AUTHORIZATION_TIMEOUT_EXPIRED);
		}, 30, TimeUnit.SECONDS);
	}

	@SuppressWarnings("unchecked")
	private void authorize(String key) {
		connection.getLogger().trace("Connect: Authorize client by key {}", key);
		ApiServer server = connection.getServer();

		AbstractClient client;
		try {
			client = server.getGate().connectClient(key);
		}
		catch (Throwable e) {
			connection.getLogger().error("Connect: Error", e);
			connection.closeSync(Protocol.CLOSE_SERVER_ERROR);
			return;
		}

		if (client == null) {
			connection.getLogger().trace("Connect: Reject");
			connection.closeSync(Protocol.CLOSE_CONNECT_REJECT);
		}
		else {
			connection.getLogger().trace("Connect: Success (clientId: {})", client.getId());
			client.setServer(server);
			server.connectClient(client, connection);
		}
	}
}
