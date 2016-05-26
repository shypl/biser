package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.Protocol;
import org.shypl.biser.csi.ProtocolException;

class ConnectionProcessorMessaging extends ConnectionProcessor {
	private final AbstractClient client;

	private State      state  = State.FLAG;
	private ByteBuffer buffer = new ByteBuffer();
	private int     messageLen;
	private boolean messageEven;

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

			case Protocol.MESSAGE_ODD:
				prepareMessage(false);
				break;
			case Protocol.MESSAGE_EVEN:
				prepareMessage(true);
				break;

			case Protocol.MESSAGE_ODD_RECEIVED:
				client.processMessageReceived(false);
				break;
			case Protocol.MESSAGE_EVEN_RECEIVED:
				client.processMessageReceived(true);
				break;

			case Protocol.CLOSE:
				connection.syncClose();
				break;

			default:
				throw new ProtocolException(String.format("Messaging: Invalid flag %02x (%c)", flag, flag));
		}
	}

	private void prepareMessage(boolean even) {
		state = State.MESSAGE_SIZE;
		messageEven = even;
		buffer.clear();
	}

	private void readMessageSize() {
		connection.read(buffer, 4 - buffer.getReadableBytes());
		if (4 == buffer.getReadableBytes()) {
			state = State.MESSAGE_BODY;
			messageLen = buffer.readInt();
			buffer.clear();
		}
	}

	private void readMessageBody() {
		connection.read(buffer, messageLen - buffer.getReadableBytes());
		if (messageLen == buffer.getReadableBytes()) {
			state = State.FLAG;
			client.receiveMessage(messageEven, buffer.readBytes());
		}
	}

	private enum State {
		FLAG, MESSAGE_SIZE, MESSAGE_BODY
	}
}
