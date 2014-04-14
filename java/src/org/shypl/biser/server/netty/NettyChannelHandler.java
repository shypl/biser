package org.shypl.biser.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.shypl.biser.server.AbstractClient;
import org.shypl.biser.server.ClientConnection;
import org.shypl.biser.server.ClientController;

@ChannelHandler.Sharable
public class NettyChannelHandler<C extends AbstractClient> extends ChannelInboundHandlerAdapter
{
	private final AttributeKey<ClientConnection> attrConnection = AttributeKey.valueOf("connection");
	private final ClientController<C> controller;

	public NettyChannelHandler(final ClientController<C> controller)
	{
		this.controller = controller;
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) throws Exception
	{
		final ClientConnection<C> connection = new ClientConnection<>(new NettyClientChannel(ctx.channel()),
			controller);
		ctx.attr(attrConnection).set(connection);
	}

	@Override
	public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception
	{
		final Attribute<ClientConnection> attr = ctx.attr(attrConnection);
		final ClientConnection connection = attr.getAndRemove();
		if (connection != null) {
			connection.close();
		}
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception
	{
		final ClientConnection connection = ctx.attr(attrConnection).get();

		if (connection == null) {
			ctx.close();
		}
		else if (msg instanceof ByteBuf) {
			final ByteBuf buf = (ByteBuf)msg;

			final byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			buf.release();
			connection.receive(data);
		}
		else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		ctx.close();
		ctx.fireExceptionCaught(cause);
	}
}
