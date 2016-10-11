package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.ConnectionCloseReason;

public class ConnectionStringLineBuffer {
	
	private final ByteBuffer buffer = new ByteBuffer();
	private boolean complete;
	
	public boolean fill(Connection connection) {
		while (connection.isReadable()) {
			byte b = connection.read();
			
			if (b == '\n') {
				complete = true;
				break;
			}
			
			if (b == (byte)0xFF) {
				connection.close(ConnectionCloseReason.NONE);
				break;
			}
			
			if (b != '\r') {
				buffer.writeByte(b);
			}
		}
		
		return complete;
	}
	
	public String read() {
		String s = buffer.readString();
		buffer.clear();
		complete = false;
		return s;
	}
}
