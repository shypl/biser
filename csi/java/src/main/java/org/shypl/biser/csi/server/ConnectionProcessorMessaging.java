package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.Protocol;
import org.shypl.biser.csi.ProtocolException;

class ConnectionProcessorMessaging extends ConnectionProcessor {
	private final AbstractClient client;
	
	private State      state  = State.FLAG;
	private ByteBuffer buffer = new ByteBuffer();
	private int messageId;
	private int messageSize;
	
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
				case MESSAGE_ID:
					readMessageId();
					break;
				case MESSAGE_SIZE:
					readMessageSize();
					break;
				case MESSAGE_BODY:
					readMessageBody();
					break;
				case OUTGOING_MESSAGE_RECEIVED:
					readOutgoingMessageReceived();
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
				client.sendLastMessageReceivedFlag();
				break;
			
			case Protocol.MESSAGE:
				prepareMessage();
				break;
			
			case Protocol.MESSAGE_RECEIVED:
				prepareOutgoingMessageReceived();
				break;
			
			case Protocol.CLOSE:
				connection.syncClose();
				break;
			
			default:
				throw new ProtocolException(String.format("Messaging: Invalid flag  0x%s (%s)", Integer.toString(flag & 0xFF, 16), (char)(flag & 0xFF)));
		}
	}
	
	private void prepareOutgoingMessageReceived() {
		state = State.OUTGOING_MESSAGE_RECEIVED;
		buffer.clear();
	}
	
	private void readOutgoingMessageReceived() {
		connection.read(buffer, 4 - buffer.getReadableBytesLength());
		if (4 == buffer.getReadableBytesLength()) {
			state = State.FLAG;
			messageId = buffer.readInt();
			buffer.clear();
			client.processOutgoingMessageReceived(messageId);
		}
	}
	
	private void prepareMessage() {
		state = State.MESSAGE_ID;
		buffer.clear();
	}
	
	private void readMessageId() {
		connection.read(buffer, 4 - buffer.getReadableBytesLength());
		if (4 == buffer.getReadableBytesLength()) {
			state = State.MESSAGE_SIZE;
			messageId = buffer.readInt();
			buffer.clear();
		}
	}
	
	private void readMessageSize() {
		connection.read(buffer, 4 - buffer.getReadableBytesLength());
		if (4 == buffer.getReadableBytesLength()) {
			state = State.MESSAGE_BODY;
			messageSize = buffer.readInt();
			buffer.clear();
		}
	}
	
	private void readMessageBody() {
		connection.read(buffer, messageSize - buffer.getReadableBytesLength());
		if (messageSize == buffer.getReadableBytesLength()) {
			state = State.FLAG;
			client.receiveMessage(messageId, buffer.readBytes());
		}
	}
	
	private enum State {
		FLAG, MESSAGE_ID, MESSAGE_SIZE, MESSAGE_BODY, OUTGOING_MESSAGE_RECEIVED
	}
}
