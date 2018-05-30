package org.shypl.biser.csi.server.netty;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.shypl.biser.csi.server.ChannelAcceptor;

public class CsiWebSocketChannelInitializer extends CsiChannelInitializer {
	private final LoggingHandler loggingHandler;

	CsiWebSocketChannelInitializer(ChannelAcceptor acceptor, LogLevel logLevel) {
		super(acceptor, logLevel);
		this.loggingHandler = logLevel == null ? null : new LoggingHandler(logLevel);
	}

	@Override
	protected void initChannel(SocketChannel ch) {
		if (loggingHandler != null) {
			ch.pipeline().addLast(loggingHandler);
		}
		ch.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
	}
}
