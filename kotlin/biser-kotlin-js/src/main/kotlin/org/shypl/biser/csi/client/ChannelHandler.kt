package org.shypl.biser.csi.client

import org.shypl.biser.io.InputData

interface ChannelHandler {
	fun handleChannelClose()
	
	fun handleChannelData(data: InputData)
	
	fun handleChannelError(error: Any)
}
