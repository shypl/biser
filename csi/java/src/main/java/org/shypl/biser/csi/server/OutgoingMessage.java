package org.shypl.biser.csi.server;

public class OutgoingMessage {
	public final int    id;
	public final byte[] data;
	
	public OutgoingMessage(int id, byte[] data) {
		this.id = id;
		this.data = data;
	}
}
