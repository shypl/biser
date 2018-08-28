package org.shypl.biser.csi.server.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.shypl.biser.csi.server.ChannelAcceptor;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ChannelAcceptor acceptor;
	
	public WebSocketChannelInitializer(ChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline()
			.addLast(new HttpServerCodec())
			.addLast(new HttpObjectAggregator(65536))
			.addLast(new WebSocketServerCompressionHandler())
			.addLast(new WebSocketServerProtocolHandler("/", null, true))
			.addLast(new WebSocketFrameHandler(acceptor));
	}
}
