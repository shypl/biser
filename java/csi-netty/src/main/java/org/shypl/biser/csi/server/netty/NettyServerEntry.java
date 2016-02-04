package org.shypl.biser.csi.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.shypl.biser.csi.ServerEntryAddress;
import org.shypl.biser.csi.server.ConnectionChannelAcceptor;
import org.shypl.biser.csi.server.ServerEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerEntry implements ServerEntry {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerEntry.class);
	private Channel channel;

	public NettyServerEntry(NettyServerEntryProvider provider, ServerEntryAddress address, ConnectionChannelAcceptor acceptor) {
		NettyChannelHandler channelHandler = new NettyChannelHandler(acceptor);

		ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(final SocketChannel ch) throws Exception {
				if (provider.hasLogLevel()) {
					ch.pipeline().addLast(new LoggingHandler(provider.getLogLevel()));
				}
				ch.pipeline().addLast(channelHandler);
			}
		};

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(provider.getBossGroup(), provider.getWorkerGroup())
			.channel(NioServerSocketChannel.class)
			.childHandler(initializer)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true);

		new Thread(() -> {
			LOGGER.info("Start...");

			try {
				final ChannelFuture future = bootstrap.bind(address.getSocket()).sync();
				channel = future.channel();
			}
			catch (InterruptedException e) {
				LOGGER.error("Bind is interrupted", e);
				close();
				return;
			}

			LOGGER.info("Started");

			try {
				channel.closeFuture().sync();
			}
			catch (InterruptedException e) {
				LOGGER.error("Close future is interrupted", e);
			}

			LOGGER.info("Stopped");
		}, "NettyServerEntryLoop").start();
	}

	@Override
	public void close() {
		LOGGER.info("Stop...");
		if (channel != null) {
			channel.close();
		}
		else {
			LOGGER.info("Stopped");
		}
	}
}
