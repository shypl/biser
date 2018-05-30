package org.shypl.biser.csi.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.shypl.biser.csi.server.ChannelAcceptor;

class CsiChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ChannelAcceptor acceptor;
	
	CsiChannelInitializer(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new CsiChannelHandler(acceptor));
	}
}
