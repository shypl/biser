package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.Protocol;
import org.shypl.biser.csi.ProtocolException;

class ConnectionProcessorMessaging extends ConnectionProcessor {
	private final AbstractClient client;
	
	private State      state  = State.FLAG;
	private ByteBuffer buffer = new ByteBuffer();
	private int messageLen;
	
	public ConnectionProcessorMessaging(AbstractClient client) {
		super();
		this.client = client;
	}
	
	@Override
	public void processData() throws ProtocolException {
		do {
			switch (state) {
				case FLAG:
					readFlag();
					break;
				case MESSAGE_SIZE:
					readMessageSize();
					break;
				case MESSAGE_BODY:
					readMessageBody();
					break;
			}
		}
		while (connection.isReadable());
	}
	
	@Override
	public void processClose(boolean breaking) {
		if (breaking) {
			client.handleConnectionBreaking();
		}
		else {
			client.handleConnectionClosed();
		}
	}
	
	private void readFlag() throws ProtocolException {
		byte flag = connection.read();
		switch (flag) {
			case Protocol.PING:
				connection.syncSend(Protocol.PING);
				break;
			
			case Protocol.MESSAGE:
				prepareMessage();
				break;
			
			case Protocol.CLOSE:
				connection.syncClose();
				break;
			
			default:
				throw new ProtocolException(String.format("Messaging: Invalid flag  0x%s (%s)", Integer.toString(flag & 0xFF, 16), (char)(flag & 0xFF)));
		}
	}
	
	private void prepareMessage() {
		state = State.MESSAGE_SIZE;
		buffer.clear();
	}
	
	private void readMessageSize() {
		connection.read(buffer, 4 - buffer.getReadableBytesLength());
		if (4 == buffer.getReadableBytesLength()) {
			state = State.MESSAGE_BODY;
			messageLen = buffer.readInt();
			buffer.clear();
		}
	}
	
	private void readMessageBody() {
		connection.read(buffer, messageLen - buffer.getReadableBytesLength());
		if (messageLen == buffer.getReadableBytesLength()) {
			state = State.FLAG;
			client.receiveMessage(buffer.readBytes());
		}
	}
	
	private enum State {
		FLAG, MESSAGE_SIZE, MESSAGE_BODY
	}
}
