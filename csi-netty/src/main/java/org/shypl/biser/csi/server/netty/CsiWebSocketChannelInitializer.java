package org.shypl.biser.csi.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
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
	
	CsiWebSocketChannelInitializer(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new WebSocketServerCompressionHandler());
		pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));
		pipeline.addLast(new CsiWebSocketFrameHandler(acceptor));
		
		ch.pipeline().addLast(new CsiChannelHandler(acceptor));
	}
}
