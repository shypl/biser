package org.shypl.biser.api.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.shypl.biser.api.server.ConnectionChannelAcceptor;
import org.shypl.biser.api.server.ConnectionChannelHandler;

@ChannelHandler.Sharable
public class NettyChannelHandler extends ChannelInboundHandlerAdapter {
	private final AttributeKey<ConnectionChannelHandler> attrHandler = AttributeKey.valueOf("connection");
	private final ConnectionChannelAcceptor acceptor;

	public NettyChannelHandler(ConnectionChannelAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		NettyConnectionChannel connectionChannel = new NettyConnectionChannel(ctx.channel());
		ConnectionChannelHandler connectionChannelHandler = acceptor.accept(connectionChannel);
		ctx.attr(this.attrHandler).set(connectionChannelHandler);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		ConnectionChannelHandler handler = ctx.attr(this.attrHandler).getAndRemove();
		if (handler != null) {
			handler.handleClose();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ConnectionChannelHandler handler = ctx.attr(this.attrHandler).get();
		if (handler == null) {
			ctx.close();
		}
		else if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf)msg;
			byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			buf.release();
			handler.handleData(data);
		}
		else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		ctx.fireExceptionCaught(cause);
	}
}