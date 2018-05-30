package org.shypl.biser.csi.client

class WebSocketChannelProvider: ChannelProvider {
	override fun openChannel(address: String, acceptor: ChannelAcceptor) {
		WebSocketChannel(address, acceptor);
	}
}