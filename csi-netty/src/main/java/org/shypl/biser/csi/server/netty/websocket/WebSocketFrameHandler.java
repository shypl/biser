package org.shypl.biser.csi.server.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.shypl.biser.csi.server.Channel;
import org.shypl.biser.csi.server.ChannelAcceptor;
import org.shypl.biser.csi.server.ChannelHandler;

import java.net.SocketAddress;

@SuppressWarnings("Duplicates")
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> implements Channel, ChannelFutureListener {
	private final Object lock = new Object();
	
	private ChannelAcceptor          acceptor;
	private io.netty.channel.Channel channel;
	private ChannelHandler           handler;
	
	private boolean opened;
	private int     writeCounter;
	private boolean closeAfterWrites;
	
	
	public WebSocketFrameHandler(ChannelAcceptor acceptor) {
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
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
		synchronized (lock) {
			if (opened) {
				ByteBuf buf = frame.content();
				byte[] bytes = new byte[buf.readableBytes()];
				buf.readBytes(bytes);
				handler.handleChannelData(bytes);
			}
			else {
				ctx.close();
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
		channel.writeAndFlush(new BinaryWebSocketFrame(buf)).addListener(this);
	}
}
