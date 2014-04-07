package org.shypl.biser.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.shypl.biser.server.ClientChannel;

class NettyClientChannel implements ClientChannel, ChannelFutureListener
{
	private final Channel channel;
	private       int     writeCounter;
	private       boolean closeFuture;

	public NettyClientChannel(final Channel channel)
	{
		this.channel = channel;
	}

	@Override
	public void write(final byte[] data)
	{
		++writeCounter;
		final ByteBuf buffer = channel.alloc().buffer(data.length);
		buffer.writeBytes(data);
		channel.writeAndFlush(buffer).addListener(this);
	}

	@Override
	public void close()
	{
		if (writeCounter == 0) {
			channel.close();
		}
		else {
			closeFuture = true;
		}
	}

	@Override
	public void operationComplete(final ChannelFuture future) throws Exception
	{
		--writeCounter;
		if (closeFuture && writeCounter == 0) {
			closeFuture = false;
			channel.close();
		}
	}
}
