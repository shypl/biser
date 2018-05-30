package org.shypl.biser.csi.client

interface ChannelProvider {
	fun openChannel(address: String, acceptor: ChannelAcceptor)
}