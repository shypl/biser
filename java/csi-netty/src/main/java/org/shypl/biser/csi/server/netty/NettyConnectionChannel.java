package org.shypl.biser.csi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.shypl.biser.csi.server.ConnectionChannel;

import java.net.SocketAddress;

public class NettyConnectionChannel implements ConnectionChannel, ChannelFutureListener {
	private final Channel channel;
	private       int     writeCounter;
	private       boolean closeFuture;

	public NettyConnectionChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return channel.remoteAddress();
	}

	@Override
	public void write(byte[] bytes) {
		++writeCounter;
		ByteBuf buffer = channel.alloc().buffer(bytes.length);
		buffer.writeBytes(bytes);
		channel.writeAndFlush(buffer).addListener(this);
	}

	@Override
	public void write(byte b) {
		++writeCounter;
		ByteBuf buffer = channel.alloc().buffer(1);
		buffer.writeByte(b);
		channel.writeAndFlush(buffer).addListener(this);
	}

	@Override
	public void close() {
		if (writeCounter == 0) {
			channel.close();
		}
		else {
			closeFuture = true;
		}
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		--writeCounter;
		if (closeFuture && writeCounter == 0) {
			closeFuture = false;
			channel.close();
		}
	}
}
