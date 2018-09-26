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
import org.shypl.biser.csi.server.netty.socket.FlashSocketChannelInitializer;
import org.shypl.biser.csi.server.netty.websocket.WebSocketChannelInitializer;
import org.shypl.biser.csi.server.netty.wss.WebSocketSecureChannelInitializer;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NettyChannelGate implements ChannelGate {
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private final LogLevel       logLevel;
	
	private volatile boolean       opened;
	private          List<Channel> channels = new ArrayList<>(2);
	
	public NettyChannelGate(EventLoopGroup bossGroup, EventLoopGroup workerGroup, LogLevel logLevel) {
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.logLevel = logLevel;
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
		
		
		if (address.isWss()) {
			open(address.host, address.wsPort, channelClass, new WebSocketSecureChannelInitializer(acceptor, address.wssCrtFile, address.wssKeyFile));
		}
		else if (address.isWs()) {
			open(address.host, address.wsPort, channelClass, new WebSocketChannelInitializer(acceptor));
		}
		
		if (address.isFs()) {
			open(address.host, address.fsPort, channelClass, new FlashSocketChannelInitializer(acceptor));
		}
	}
	
	@Override
	public void close() {
		if (!opened) {
			throw new IllegalStateException("Gate is not opened");
		}
		
		for (Channel channel : channels) {
			try {
				channel.close().sync();
			}
			catch (InterruptedException e) {
				LoggerFactory.getLogger(NettyChannelGate.class).error("Close is interrupted", e);
			}
		}
		opened = false;
	}
	
	private void open(String host, int port, Class<? extends ServerChannel> channelClass, ChannelHandler channelHandler) {
		ServerBootstrap bootstrap = new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(channelClass);
		
		if (logLevel != null) {
			bootstrap.childHandler(new LoggingHandler(logLevel));
		}
		
		bootstrap.childHandler(channelHandler)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true);
		
		try {
			LoggerFactory.getLogger(NettyChannelGate.class).info("Server bind address {}:{}", host, port);
			channels.add(bootstrap.bind(host, port).sync().channel());
		}
		catch (InterruptedException e) {
			throw new RuntimeException("Bind is interrupted", e);
		}
	}
}
