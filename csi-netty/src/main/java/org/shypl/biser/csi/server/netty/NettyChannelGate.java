package org.shypl.biser.csi.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import org.shypl.biser.csi.Address;
import org.shypl.biser.csi.server.ChannelAcceptor;
import org.shypl.biser.csi.server.ChannelGate;
import org.slf4j.LoggerFactory;

public class NettyChannelGate implements ChannelGate {
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private final LogLevel logLevel;
	private boolean useWebSocket = false;

	private volatile boolean opened;
	private Channel channel;

	public NettyChannelGate(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
		this(bossGroup, workerGroup, null);
	}

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

		ChannelHandler childHandler = useWebSocket ? new CsiWebSocketChannelInitializer(acceptor, logLevel) : new CsiChannelInitializer(acceptor, logLevel);

		ServerBootstrap bootstrap = new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(channelClass)
			.childHandler(childHandler)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true);

		try {
			channel = bootstrap.bind(address.getSocket()).sync().channel();
		} catch (InterruptedException e) {
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
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(NettyChannelGate.class).error("Close is interrupted", e);
		}

		opened = false;
	}

	public NettyChannelGate setUseWebSocket(boolean useWebSocket) throws IllegalStateException {
		if (opened) {
			throw new IllegalStateException("Gate is opened");
		}
		this.useWebSocket = useWebSocket;
		return this;
	}
}
