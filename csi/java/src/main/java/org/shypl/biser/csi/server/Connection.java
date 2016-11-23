package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.ConnectionCloseReason;
import org.shypl.biser.csi.ProtocolException;
import org.shypl.common.concurrent.Worker;
import org.shypl.common.concurrent.WrappedTaskWorker;
import org.shypl.common.slf4j.PrefixedLoggerProxy;
import org.shypl.common.util.Cancelable;
import org.shypl.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

class Connection implements ChannelHandler {
	private static long ID_COUNTER;
	
	private synchronized static long nextId() {
		return ++ID_COUNTER;
	}
	
	private final long       id;
	private final Server     server;
	private final Channel    channel;
	private final Logger     logger;
	private final Worker     worker;
	private final Cancelable activityTimeout;
	
	private volatile boolean             opened;
	private          boolean             closed;
	private          boolean             activity;
	private          ConnectionProcessor processor;
	
	private byte[] readerData;
	private int    readerCursor;
	
	public Connection(Server server, Channel channel) {
		id = nextId();
		
		this.server = server;
		this.channel = channel;
		logger = new PrefixedLoggerProxy(LoggerFactory.getLogger(Connection.class), "[" + server.getApi().getName() + '#' + channel.getRemoteAddress() + "] ");
		worker = new WrappedTaskWorker(server.getExecutorsProvider().getConnectionExecutorService(), this::wrapWorkerTask);
		
		logger.debug("Open");
		
		opened = true;
		setProcessor(new ConnectionProcessorReception());
		activityTimeout = worker.scheduleTaskPeriodic(this::checkActivity, server.getSettings().getConnectionActivityTimeout(), TimeUnit.SECONDS);
	}
	
	public void cancelCheckActivityTimeout() {
		activityTimeout.cancel();
	}
	
	public boolean isOpened() {
		return opened;
	}
	
	public long getId() {
		return id;
	}
	
	public SocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}
	
	public Server getServer() {
		return server;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public void setProcessor(ConnectionProcessor processor) {
		this.processor = processor;
		processor.init(this);
	}
	
	@Override
	public void handleChannelData(byte[] bytes) {
		worker.addTask(() -> {
			if (logger.isTraceEnabled()) {
				logger.trace(">> {}", StringUtils.toString(bytes));
			}
			
			if (server.getSettings().isEmulateDelayInConnectionDataProcessing()) {
				try {
					Thread.sleep(server.getSettings().getEmulateDelayInConnectionDataProcessingMillis());
				}
				catch (InterruptedException e) {
					logger.error("EmulateDelayInConnectionDataProcessing interrupted", e);
					syncClose(ConnectionCloseReason.SERVER_ERROR);
				}
			}
			
			activity = true;
			readerData = bytes;
			readerCursor = 0;
			
			while (isReadable()) {
				try {
					processor.processData();
				}
				catch (ProtocolException e) {
					logger.debug("Protocol broken", e);
					syncClose(ConnectionCloseReason.PROTOCOL_BROKEN);
				}
				catch (Throwable e) {
					logger.error("Error on process data", e);
					syncClose(ConnectionCloseReason.SERVER_ERROR);
				}
			}
			readerData = null;
		});
	}
	
	@Override
	public void handleChannelClose() {
		worker.addTask(() -> {
			activityTimeout.cancel();
			
			logger.debug("Close");
			
			boolean breaking = opened;
			
			opened = false;
			closed = true;
			
			processor.processClose(breaking);
			server.releaseConnection(this);
			processor.destroy();
			processor = null;
		});
	}
	
	public void send(byte b) {
		worker.addTask(() -> syncSend(b));
	}
	
	public void send(byte[] bytes) {
		worker.addTask(() -> syncSend(bytes));
	}
	
	public void close(ConnectionCloseReason reason) {
		worker.addTask(() -> syncClose(reason));
	}
	
	void syncSend(byte[] bytes) {
		writeToChannel(bytes);
	}
	
	void syncSend(byte b) {
		writeToChannel(b);
	}
	
	void syncClose() {
		if (opened) {
			opened = false;
			channel.close();
		}
	}
	
	void syncClose(ConnectionCloseReason reason) {
		if (opened) {
			opened = false;
			writeToChannel(ConnectionCloseReason.getProtocolFlag(reason));
			channel.close();
		}
	}
	
	byte read() {
		return readerData[readerCursor++];
	}
	
	int read(ByteBuffer target, int length) {
		length = Math.min(getReadableBytesLength(), length);
		
		if (length > 0) {
			target.writeBytes(readerData, readerCursor, length);
			readerCursor += length;
		}
		return length;
	}
	
	int read(ByteBuffer target) {
		return read(target, getReadableBytesLength());
	}
	
	int read(byte[] target, int offset) {
		return read(target, offset, target.length - offset);
	}
	
	int read(byte[] target, int offset, int length) {
		length = Math.min(readerData.length - readerCursor, length);
		if (length > 0) {
			System.arraycopy(readerData, readerCursor, target, offset, length);
			readerCursor += length;
		}
		return length;
	}
	
	boolean isReadable() {
		return opened && readerData.length > readerCursor;
	}
	
	int getReadableBytesLength() {
		return readerData.length - readerCursor;
	}
	
	private void checkActivity() {
		if (activity) {
			activity = false;
		}
		else {
			logger.debug("Activity timeout expired");
			activityTimeout.cancel();
			writeToChannel(ConnectionCloseReason.getProtocolFlag(ConnectionCloseReason.ACTIVITY_TIMEOUT_EXPIRED));
			channel.close();
		}
	}
	
	private void writeToChannel(byte b) {
		if (logger.isTraceEnabled()) {
			logger.trace("<< {}", '[' + StringUtils.toString(b) + ']');
		}
		channel.write(b);
	}
	
	private void writeToChannel(byte[] bytes) {
		if (logger.isTraceEnabled()) {
			logger.trace("<< {}", StringUtils.toString(bytes));
		}
		channel.write(bytes);
	}
	
	private Runnable wrapWorkerTask(Runnable task) {
		return () -> {
			if (closed) {
				logger.debug("Can't run task on closed connection");
			}
			else {
				task.run();
			}
		};
	}
}
