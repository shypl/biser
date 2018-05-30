package org.shypl.biser.csi;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@SuppressWarnings("WeakerAccess")
public class Address {
	@Deprecated
	public static Address factorySocket(String address) {
		int p = address.indexOf(":");
		return factorySocket(new InetSocketAddress(address.substring(0, p), Integer.parseInt(address.substring(p + 1))));
	}

	public static Address factorySocket(SocketAddress address) {
		return new Address(address, false);
	}

	@SuppressWarnings("unused")
	public static Address factoryWebSocket(SocketAddress address) {
		return new Address(address, true);
	}

	private final SocketAddress socket;
	private final boolean webSocket;

	private Address(SocketAddress socket, boolean webSocket) {
		this.socket = socket;
		this.webSocket = webSocket;
	}

	public boolean isWebSocket() {
		return webSocket;
	}

	public SocketAddress getSocket() {
		return socket;
	}
}
