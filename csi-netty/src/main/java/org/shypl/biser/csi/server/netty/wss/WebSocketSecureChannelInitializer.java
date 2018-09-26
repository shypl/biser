package org.shypl.biser.csi.server.netty.wss;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.shypl.biser.csi.server.ChannelAcceptor;
import org.shypl.biser.csi.server.netty.websocket.WebSocketFrameHandler;

import java.io.File;

public class WebSocketSecureChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ChannelAcceptor acceptor;
	private final File            sslCrtFile;
	private final File            sslKeyFile;
	
	public WebSocketSecureChannelInitializer(ChannelAcceptor acceptor, String sslCrtFile, String sslKeyFile) {
		this.acceptor = acceptor;
		this.sslCrtFile = new File(sslCrtFile);
		this.sslKeyFile = new File(sslKeyFile);
	}
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		SslContext sslCtx = SslContextBuilder.forServer(sslCrtFile, sslKeyFile).build();
		
		ch.pipeline()
			.addLast(sslCtx.newHandler(ch.alloc()))
			.addLast(new HttpServerCodec())
			.addLast(new HttpObjectAggregator(65536))
			.addLast(new WebSocketServerCompressionHandler())
			.addLast(new WebSocketServerProtocolHandler("/", null, true))
			.addLast(new WebSocketFrameHandler(acceptor));
	}
}
