package org.shypl.biser.csi.client

import org.shypl.biser.csi.CsiException

class WebSocketChannelProvider: ChannelProvider {
	override fun openChannel(address: String, acceptor: ChannelAcceptor) {
		try {
			WebSocketChannel(address, acceptor);
		}
		catch (e: dynamic) {
			acceptor.failOpenChannel(CsiException(e))
		}
	}
}