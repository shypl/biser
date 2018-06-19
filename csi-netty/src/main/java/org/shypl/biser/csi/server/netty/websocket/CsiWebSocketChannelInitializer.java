package org.shypl.biser.csi.server.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.shypl.biser.csi.server.ChannelAcceptor;

public class CsiWebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ChannelAcceptor acceptor;
	
	public CsiWebSocketChannelInitializer(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline()
			.addLast(new LoggingHandler(LogLevel.TRACE))
			.addLast(new HttpServerCodec())
			.addLast(new HttpObjectAggregator(65536))
			.addLast(new WebSocketServerCompressionHandler())
			.addLast(new WebSocketServerProtocolHandler("/", null, true))
			.addLast(new CsiWebSocketFrameHandler(acceptor));
	}
}
