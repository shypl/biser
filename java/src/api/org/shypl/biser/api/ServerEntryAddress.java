package org.shypl.biser.api;

import java.net.SocketAddress;
import java.util.Objects;

public class ServerEntryAddress {
	private SocketAddress socket;

	public ServerEntryAddress(SocketAddress socket) {
		Objects.requireNonNull(socket);
		this.socket = socket;
	}

	public SocketAddress getSocket() {
		return socket;
	}

	public boolean isSocket() {
		return socket != null;
	}

	@Override
	public String toString() {
		return socket.toString();
	}
}
