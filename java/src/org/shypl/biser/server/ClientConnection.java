package org.shypl.biser.server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ClientConnection<C extends AbstractClient>
{
	private final static byte[] POLICY_REQUEST = ("policy-file-request/>" + (char)0).getBytes();

	private final static byte MARK_POLICY = '<';
	private final static byte MARK_MSG_1  = 0x01;
	private final static byte MARK_MSG_2  = 0x02;
	private final static byte MARK_MSG_3  = 0x03;
	private final static byte MARK_MSG_4  = 0x04;
	private final static byte MARK_CLOSE  = 0x00;
	private final static byte MARK_SID    = (byte)0xFE;
	private final static byte MARK_PING   = (byte)0xFF;

	private final static byte[] PING = new byte[]{MARK_PING};

	private final static byte STATE_MARK     = 0;
	private final static byte STATE_POLICY   = 1;
	private final static byte STATE_SID      = 2;
	private final static byte STATE_MSG_SIZE = 3;
	private final static byte STATE_MSG_BODY = 4;

	private final Object lock = new Object();
	private final ClientChannel       channel;
	private final ClientController<C> controller;
	private final C                   client;
	private final CheckPing           checkPing;
	private       byte                state;
	private       boolean             closed;
	private       ByteBuffer          buffer;
	private       int                 msgLength;
	private       int                 msgSize;
	private       byte[]              msgBody;
	private boolean pinged = true;

	public ClientConnection(final ClientChannel channel, final ClientController<C> controller)
	{
		this.channel = channel;
		this.controller = controller;
		client = controller.clientFactory.createClient(this, controller);
		checkPing = new CheckPing();

		state = STATE_MARK;
		checkPing.start();
	}

	public void close()
	{
		synchronized (lock) {
			if (closed) {
				return;
			}
			closed = true;
		}

		channel.close();
		client.handleDisconnect();
	}

	public void send(final byte[] data)
	{
		synchronized (lock) {
			if (closed) {
				return;
			}
			channel.write(data);
		}
	}

	public void receive(final byte[] data)
	{
		synchronized (lock) {
			if (closed) {
				return;
			}

			pinged = true;
			checkPing.stop();
			buffer = new ByteBuffer(data);

			while (buffer.isReadable()) {
				switch (state) {
					case STATE_MARK:
						readMark();
						break;

					case STATE_MSG_SIZE:
						readMsgSize();
						break;

					case STATE_MSG_BODY:
						readMsgBody();
						break;

					case STATE_SID:
						readSid();
						break;

					case STATE_POLICY:
						readPolicy();
						break;
				}
				if (closed) {
					break;
				}
			}

			buffer = null;
			if (!closed) {
				checkPing.start();
			}
		}
	}

	private void readMark()
	{
		final byte b = buffer.readByte();
		switch (b) {
			case MARK_MSG_1:
				newMsgSize(1);
				break;

			case MARK_MSG_2:
				newMsgSize(2);
				break;

			case MARK_MSG_3:
				newMsgSize(3);
				break;

			case MARK_MSG_4:
				newMsgSize(4);
				break;

			case MARK_PING:
				send(PING);
				break;

			case MARK_SID:
				newSid();
				break;

			case MARK_CLOSE:
				close();
				break;

			case MARK_POLICY:
				newPolicy();
				break;

			default:
				throw new BadRequestFormatException("Illegal message mark: " + Byte.toString(b));
		}
	}

	private void newMsgSize(final int length)
	{
		msgSize = 0;
		msgLength = length;
		readMsgSize();
	}

	private void readMsgSize()
	{
		while (msgLength > 0 && buffer.isReadable()) {
			msgSize |= (buffer.readByte() & 0xFF) << (8 * --msgLength);
		}

		if (msgLength != 0) {
			state = STATE_MSG_SIZE;
			return;
		}

		msgBody = new byte[msgSize];
		msgLength = 0;

		readMsgBody();
	}

	private void readMsgBody()
	{
		final int bufferSize = buffer.size();
		int remaining = msgSize - msgLength;

		if (remaining > bufferSize) {
			remaining = bufferSize;
		}

		buffer.readBytes(msgBody, msgLength, remaining);

		msgLength += remaining;

		if (msgLength != msgSize) {
			state = STATE_MSG_BODY;
			return;
		}

		state = STATE_MARK;

		controller.router.receiveMessage(client, msgBody);
		msgBody = null;
	}

	private void newSid()
	{
		throw new IllegalStateException("Not supported");
	}

	private void readSid()
	{
		throw new IllegalStateException("Not supported");
	}

	private void newPolicy()
	{
		state = STATE_POLICY;
		msgLength = 0;
		client.authorize();
		readPolicy();
	}

	private void readPolicy()
	{
		while (buffer.isReadable()) {
			if (buffer.readByte() == POLICY_REQUEST[msgLength]) {
				if (++msgLength == POLICY_REQUEST.length) {
					send(controller.getPolicyResponse());
					close();
					return;
				}
			}
			else {
				close();
				throw new BadRequestFormatException("Bad policy request");
			}
		}
	}

	private class CheckPing implements Runnable
	{
		private ScheduledFuture<?> future;

		@Override
		public void run()
		{
			synchronized (lock) {
				if (closed) {
					return;
				}
				if (pinged) {
					pinged = false;
					start();
				}
				else {
					close();
				}
			}
		}

		public void start()
		{
			future = controller.scheduler.schedule(this, controller.getTimeout(), TimeUnit.SECONDS);
		}

		public void stop()
		{
			if (future != null) {
				future.cancel(false);
				future = null;
			}
		}
	}
}
