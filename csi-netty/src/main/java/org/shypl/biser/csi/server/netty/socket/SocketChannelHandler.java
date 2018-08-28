package org.shypl.biser.csi.server.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.shypl.biser.csi.server.Channel;
import org.shypl.biser.csi.server.ChannelAcceptor;
import org.shypl.biser.csi.server.ChannelHandler;

import java.net.SocketAddress;

public class SocketChannelHandler extends ChannelInboundHandlerAdapter implements Channel, ChannelFutureListener {
	
	private final Object lock = new Object();
	
	private ChannelAcceptor          acceptor;
	private io.netty.channel.Channel channel;
	private ChannelHandler           handler;
	
	private boolean opened;
	private int     writeCounter;
	private boolean closeAfterWrites;
	
	public SocketChannelHandler(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	@Override
	public SocketAddress getRemoteAddress() {
		synchronized (lock) {
			return opened ? channel.remoteAddress() : null;
		}
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		synchronized (lock) {
			opened = true;
			channel = ctx.channel();
			handler = acceptor.accept(this);
			acceptor = null;
		}
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		synchronized (lock) {
			if (opened) {
				opened = false;
				handler.handleChannelClose();
				handler = null;
				channel = null;
				acceptor = null;
			}
		}
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		synchronized (lock) {
			if (opened) {
				ByteBuf buf = (ByteBuf)msg;
				byte[] bytes = new byte[buf.readableBytes()];
				buf.readBytes(bytes);
				buf.release();
				handler.handleChannelData(bytes);
			}
			else {
				ctx.close();
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
	
	@Override
	public void write(byte[] bytes) {
		synchronized (lock) {
			if (opened) {
				incrementWriteCounter();
				
				ByteBuf buf = allocBuf(bytes.length);
				buf.writeBytes(bytes);
				writeBuf(buf);
			}
		}
	}
	
	@Override
	public void write(byte b) {
		synchronized (lock) {
			if (opened) {
				incrementWriteCounter();
				
				ByteBuf buf = allocBuf(1);
				buf.writeByte(b);
				writeBuf(buf);
			}
		}
	}
	
	@Override
	public void close() {
		synchronized (lock) {
			if (opened) {
				if (writeCounter == 0) {
					channel.close();
				}
				else {
					closeAfterWrites = true;
				}
			}
		}
	}
	
	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		synchronized (lock) {
			--writeCounter;
			if (closeAfterWrites && writeCounter == 0 && opened) {
				channel.close();
			}
		}
	}
	
	private void incrementWriteCounter() {
		++writeCounter;
	}
	
	private ByteBuf allocBuf(int size) {
		return channel.alloc().buffer(size);
	}
	
	private void writeBuf(ByteBuf buf) {
		channel.writeAndFlush(buf).addListener(this);
	}
}