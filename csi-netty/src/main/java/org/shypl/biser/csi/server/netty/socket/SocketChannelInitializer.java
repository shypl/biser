package org.shypl.biser.csi.server.netty.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.shypl.biser.csi.server.ChannelAcceptor;

public class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final ChannelAcceptor acceptor;

	public SocketChannelInitializer(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new SocketChannelHandler(acceptor));
	}
}
