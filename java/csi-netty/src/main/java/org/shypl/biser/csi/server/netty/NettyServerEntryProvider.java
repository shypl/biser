package org.shypl.biser.csi.server.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.shypl.biser.csi.ServerEntryAddress;
import org.shypl.biser.csi.server.ConnectionChannelAcceptor;
import org.shypl.biser.csi.server.ServerEntry;
import org.shypl.biser.csi.server.ServerEntryProvider;

public class NettyServerEntryProvider implements ServerEntryProvider {

	static {
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	}

	private final NioEventLoopGroup bossGroup;
	private final NioEventLoopGroup workerGroup;
	private       LogLevel          logLevel;

	public NettyServerEntryProvider() {
		this(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 3);
	}

	public NettyServerEntryProvider(int bossGroupThreads, int workerGroupThreads) {
		bossGroup = new NioEventLoopGroup(bossGroupThreads);
		workerGroup = new NioEventLoopGroup(workerGroupThreads);
	}

	public NioEventLoopGroup getBossGroup() {
		return bossGroup;
	}

	public NioEventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	@Override
	public ServerEntry open(ServerEntryAddress address, ConnectionChannelAcceptor connectionAcceptor) {
		return new NettyServerEntry(this, address, connectionAcceptor);
	}

	public void close() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();

		try {
			workerGroup.terminationFuture().sync();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			bossGroup.terminationFuture().sync();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	boolean hasLogLevel() {
		return logLevel != null;
	}
}
