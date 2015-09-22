package org.shypl.biser.api.server;

import org.shypl.biser.api.Protocol;
import org.shypl.biser.api.ProtocolException;
import org.shypl.common.concurrent.TaskQueue;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

class Connection implements ConnectionChannelHandler {
	private static int ID_COUNTER;

	private synchronized static long nextId() {
		return ++ID_COUNTER;
	}

	private final long              id;
	private final Logger            logger;
	private final ApiServer         server;
	private final ConnectionChannel channel;

	private final TaskQueue          taskQueue;
	private       ConnectionStrategy strategy;
	private volatile boolean opened = true;
	private byte[] readerData;

	private int readerIndex;

	public Connection(ApiServer server, ConnectionChannel channel) {
		id = nextId();
		logger = new PrefixedLoggerProxy(LoggerFactory.getLogger(Connection.class), "<" + channel.getRemoteAddress() + "> ");
		logger.debug("Accept connection");

		this.server = server;
		this.channel = channel;

		taskQueue = new TaskQueue(server.getExecutor());
	}

	public long getId() {
		return id;
	}

	public boolean isOpened() {
		return opened;
	}

	public Logger getLogger() {
		return logger;
	}

	public ApiServer getServer() {
		return server;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void close(byte reason) {
		if (opened) {
			taskQueue.add(() -> closeSync(reason));
		}
	}

	public void write(byte b) {
		if (opened) {
			taskQueue.add(() -> writeSync(b));
		}
	}

	public void write(byte[] bytes) {
		if (opened) {
			taskQueue.add(() -> writeSync(bytes));
		}
	}

	@Override
	public void handleData(byte[] data) {
		taskQueue.add(() -> handleDataSync(data));
	}

	@Override
	public void handleClose() {
		if (opened) {
			taskQueue.add(() -> releaseSync(true));
		}
	}

	public SocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	void setStrategy(ConnectionStrategy strategy) {
		if (opened) {
			this.strategy = strategy;
			strategy.setConnection(this);
		}
	}

	void closeBrokenSync() {
		if (opened) {
			releaseSync(true);
			channel.close();
		}
	}

	void closeSync() {
		if (opened) {
			releaseSync(false);
			channel.close();
		}
	}

	void closeSync(byte reason) {
		if (opened) {
			releaseSync(false);
			channel.write(reason);
			channel.close();
		}
	}

	void writeSync(byte b) {
		if (opened) {
			channel.write(b);
		}
	}

	void writeSync(byte[] bytes) {
		if (opened) {
			channel.write(bytes);
		}
	}

	byte readSync() {
		return readerData[readerIndex++];
	}

	int readSync(byte[] dst, int dstIndex) {
		return readSync(dst, dstIndex, dst.length - dstIndex);
	}

	int readSync(byte[] dst, int dstIndex, int length) {
		int remain = readerData.length - readerIndex;
		if (length > remain) {
			length = remain;
		}
		if (length > 0) {
			System.arraycopy(readerData, readerIndex, dst, dstIndex, length);
			readerIndex += length;
		}
		return length;
	}

	boolean isReadableSync() {
		return opened && readerData.length > readerIndex;
	}

	private void handleDataSync(byte[] data) {
		readerData = data;
		readerIndex = 0;
		while (isReadableSync()) {
			try {
				strategy.handleData();
			}
			catch (ProtocolException e) {
				logger.error("Protocol broken", e);
				closeSync(Protocol.CLOSE_PROTOCOL_BROKEN);
			}
		}
		readerData = null;
	}

	private void releaseSync(boolean broken) {
		opened = false;
		logger.debug("Release connection (broken: {})", broken);
		server.releaseConnection(this);
		strategy.handleClose(broken);
		strategy = null;
		readerData = null;
	}
}
