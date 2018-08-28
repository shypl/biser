package org.shypl.biser.csi;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@SuppressWarnings("WeakerAccess")
public class Address {
	public enum Type {
		SOCKET, WEB_SOCKET, WEB_SOCKET_SECURE
	}
	
	public static Address factory(String address) {
		Type type;
		if (address.startsWith("wss://")) {
			type = Type.WEB_SOCKET_SECURE;
			address = address.substring(6);
		}
		else if (address.startsWith("ws://")) {
			type = Type.WEB_SOCKET;
			address = address.substring(5);
		}
		else {
			type = Type.SOCKET;
		}
		
		int p = address.indexOf(":");
		return new Address(new InetSocketAddress(address.substring(0, p), Integer.parseInt(address.substring(p + 1))), type);
	}
	
	public final SocketAddress socket;
	public final Type          type;
	
	private Address(SocketAddress socket, Type type) {
		this.socket = socket;
		this.type = type;
	}
}
