package org.shypl.biser.csi;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Address {
	public static Address factorySocket(String address) {
		int p = address.indexOf(":");
		return factorySocket(new InetSocketAddress(address.substring(0, p), Integer.parseInt(address.substring(p + 1))));
	}

	public static Address factorySocket(SocketAddress address) {
		Address self = new Address();
		self.socket = address;
		return self;
	}
	
	public static Address factoryWebSocket(String address) {
		Address a = factorySocket(address);
		a.webSocket = true;
		return a;
	}

	private SocketAddress socket;
	private Boolean webSocket;

	private Address() {
	}
	
	public boolean isWebSocket() {
		return webSocket && isSocket();
	}
	
	public boolean isSocket() {
		return socket != null;
	}

	public SocketAddress getSocket() {
		return socket;
	}
}
