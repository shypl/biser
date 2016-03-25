package org.shypl.biser.csi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.shypl.biser.csi.server.Channel;
import org.shypl.biser.csi.server.ChannelAcceptor;
import org.shypl.biser.csi.server.ChannelHandler;

import java.net.SocketAddress;

class CsiChannelHandler extends ChannelInboundHandlerAdapter implements Channel, ChannelFutureListener {

	private ChannelAcceptor          acceptor;
	private io.netty.channel.Channel channel;
	private ChannelHandler           handler;

	private int     writeCounter;
	private boolean closeAfterWrites;

	CsiChannelHandler(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return channel.remoteAddress();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		channel = ctx.channel();
		handler = acceptor.accept(this);
		acceptor = null;
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		if (handler != null) {
			handler.handleChannelClose();
			handler = null;
			channel = null;
			acceptor = null;
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (handler == null) {
			ctx.close();
		}
		else {
			ByteBuf buf = (ByteBuf)msg;
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			buf.release();
			handler.handleChannelData(bytes);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

	@Override
	public void write(byte[] bytes) {
		incrementWriteCounter();

		ByteBuf buf = allocBuf(bytes.length);
		buf.writeBytes(bytes);
		writeBuf(buf);
	}

	@Override
	public void write(byte b) {
		incrementWriteCounter();

		ByteBuf buf = allocBuf(1);
		buf.writeByte(b);
		writeBuf(buf);
	}

	@Override
	public void close() {
		if (writeCounter == 0) {
			channel.close();
		}
		else {
			closeAfterWrites = true;
		}
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		--writeCounter;
		if (closeAfterWrites && writeCounter == 0) {
			channel.close();
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
		buf.release();
	}
}