package org.shypl.biser.csi.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.shypl.biser.csi.server.ChannelAcceptor;

class CsiChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final ChannelAcceptor acceptor;
	private final LoggingHandler  loggingHandler;

	CsiChannelInitializer(ChannelAcceptor acceptor, LogLevel logLevel) {
		this.acceptor = acceptor;
		loggingHandler = logLevel == null ? null : new LoggingHandler(logLevel);
	}

	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		if (loggingHandler != null) {
			ch.pipeline().addLast(loggingHandler);
		}
		ch.pipeline().addLast(new CsiChannelHandler(acceptor));
	}
}
