package org.shypl.biser.csi.client

interface ChannelAcceptor {
	fun acceptChannel(channel: Channel): ChannelHandler
	
	fun failOpenChannel(error: Throwable)
}