package org.shypl.biser.api.server;

import org.apache.commons.codec.binary.Hex;
import org.shypl.biser.api.ByteArrayBuilder;
import org.shypl.biser.api.Protocol;
import org.shypl.biser.api.ProtocolException;
import org.shypl.common.concurrent.ScheduledTask;
import org.shypl.common.util.ByteUtils;

import java.util.concurrent.TimeUnit;

class ConnectionStrategyMessaging extends ConnectionStrategy {
	enum State {
		FLAG, MESSAGE_SIZE, MESSAGE_BODY
	}

	private static final int CHECK_PING_SECONDS = 60;

	private final AbstractClient client;
	private State state = State.FLAG;
	private ScheduledTask checkPingTask;
	private boolean       pinged;
	private boolean       messageEven;
	private byte[]        buffer;
	private int           bufferIndex;

	public ConnectionStrategyMessaging(AbstractClient client) {
		this.client = client;
	}

	@Override
	public void handleData() throws ProtocolException {
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
		while (connection.isReadableSync());
	}

	@Override
	public void handleClose(boolean broken) {
		cancelCheckPingTask();
		if (broken) {
			client.handleConnectionBroken();
		}
	}

	@Override
	void setConnection(Connection connection) {
		super.setConnection(connection);
		byte[] sid = connection.getServer().getClientConnectionSid(client);

		if (connection.getLogger().isTraceEnabled()) {
			connection.getLogger().trace("Client: Connection established (clientId: {}, connectionId: {}, sid: {})",
				client.getId(), client.getConnection().getId(), Hex.encodeHexString(sid));
		}

		connection.write(
			new ByteArrayBuilder(1 + 8 + sid.length) // sid.length = 16
				.add(Protocol.CONNECT_SUCCESS)
				.add(client.getId())
				.add(sid)
				.build()
		);

		checkPingTask = connection.getTaskQueue().schedulePeriodic(this::checkPing, CHECK_PING_SECONDS, CHECK_PING_SECONDS, TimeUnit.SECONDS);
	}

	private void cancelCheckPingTask() {
		if (checkPingTask != null) {
			checkPingTask.cancel();
			checkPingTask = null;
		}
	}

	private void readFlag() throws ProtocolException {
		byte flag = connection.readSync();
		switch (flag) {
			case Protocol.PING:
				pinged = true;
				connection.writeSync(Protocol.PING);
				break;
			case Protocol.MESSAGE_ODD:
				setStateMessageSize(false);
				break;
			case Protocol.MESSAGE_EVEN:
				setStateMessageSize(true);
				break;
			case Protocol.MESSAGE_ODD_RECEIVED:
				client.completeMessageSend(false);
				break;
			case Protocol.MESSAGE_EVEN_RECEIVED:
				client.completeMessageSend(true);
				break;
			case Protocol.CLOSE:
				connection.closeSync();
				client.disconnect(Protocol.CLOSE);
				break;
			default:
				throw new ProtocolException("Client: Invalid flag " + String.format("%02x", flag) + "(" + ((char)flag) + ")");
		}
	}

	private void setStateMessageSize(boolean even) {
		state = State.MESSAGE_SIZE;
		messageEven = even;
		bufferIndex = 0;
		buffer = new byte[4];
	}

	private void readMessageSize() {
		bufferIndex += connection.readSync(buffer, bufferIndex);
		if (bufferIndex == 4) {
			state = State.MESSAGE_BODY;
			buffer = new byte[ByteUtils.readInt(buffer, 0)];
			bufferIndex = 0;
		}
	}

	private void readMessageBody() {
		bufferIndex += connection.readSync(buffer, bufferIndex);
		if (bufferIndex == buffer.length) {
			state = State.FLAG;
			client.receiveMessage(messageEven, buffer);
			buffer = null;
		}
	}

	private void checkPing() {
		if (pinged) {
			pinged = false;
		}
		else {
			connection.closeBrokenSync();
		}
	}
}
