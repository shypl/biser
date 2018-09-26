package org.shypl.biser.csi.server.netty.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.shypl.biser.csi.server.ChannelAcceptor;

public class FlashSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final ChannelAcceptor acceptor;

	public FlashSocketChannelInitializer(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new FlashSocketChannelHandler(acceptor));
	}
}
