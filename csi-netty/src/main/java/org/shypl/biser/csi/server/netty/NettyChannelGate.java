package org.shypl.biser.csi.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.shypl.biser.csi.Address;
import org.shypl.biser.csi.server.ChannelAcceptor;
import org.shypl.biser.csi.server.ChannelGate;
import org.shypl.biser.csi.server.netty.socket.SocketChannelInitializer;
import org.shypl.biser.csi.server.netty.websocket.WebSocketChannelInitializer;
import org.shypl.biser.csi.server.netty.wss.WebSocketSecureChannelInitializer;
import org.slf4j.LoggerFactory;

import java.io.File;

public class NettyChannelGate implements ChannelGate {
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private final LogLevel       logLevel;
	private final File           sslCrtFile;
	private final File           sslKeyFile;
	
	private volatile boolean opened;
	private          Channel channel;
	
	public NettyChannelGate(EventLoopGroup bossGroup, EventLoopGroup workerGroup, LogLevel logLevel, File sslCrtFile, File sslKeyFile) {
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.logLevel = logLevel;
		this.sslCrtFile = sslCrtFile;
		this.sslKeyFile = sslKeyFile;
	}
	
	@Override
	public void open(Address address, ChannelAcceptor acceptor) {
		if (opened) {
			throw new IllegalStateException("Gate already opened");
		}
		opened = true;
		
		Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
		if (Epoll.isAvailable() && bossGroup instanceof EpollEventLoopGroup) {
			channelClass = EpollServerSocketChannel.class;
		}
		
		ChannelHandler childHandler;
		switch (address.type) {
			case SOCKET:
				childHandler = new SocketChannelInitializer(acceptor);
				break;
			case WEB_SOCKET:
				childHandler = new WebSocketChannelInitializer(acceptor);
				break;
			case WEB_SOCKET_SECURE:
				childHandler = new WebSocketSecureChannelInitializer(acceptor, sslCrtFile, sslKeyFile);
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		ServerBootstrap bootstrap = new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(channelClass);
		
		if (logLevel != null) {
			bootstrap.childHandler(new LoggingHandler(logLevel));
		}
		
		bootstrap.childHandler(childHandler)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true);
		
		try {
			channel = bootstrap.bind(address.socket).sync().channel();
		}
		catch (InterruptedException e) {
			throw new RuntimeException("Bind is interrupted", e);
		}
	}
	
	@Override
	public void close() {
		if (!opened) {
			throw new IllegalStateException("Gate is not opened");
		}
		
		try {
			channel.close().sync();
		}
		catch (InterruptedException e) {
			LoggerFactory.getLogger(NettyChannelGate.class).error("Close is interrupted", e);
		}
		
		opened = false;
	}
}
