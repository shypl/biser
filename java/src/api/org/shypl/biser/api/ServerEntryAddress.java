package org.shypl.biser.api;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

public class ServerEntryAddress {

	private static SocketAddress extractSocketAddress(String address) {
		Objects.requireNonNull(address);
		int i = address.indexOf(':');
		return new InetSocketAddress(address.substring(0, i), Integer.parseInt(address.substring(i + 1)));
	}

	private SocketAddress socket;

	public ServerEntryAddress(String address) {
		this(extractSocketAddress(address));
	}

	public ServerEntryAddress(SocketAddress address) {
		Objects.requireNonNull(address);
		this.socket = address;
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
