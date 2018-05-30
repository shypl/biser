package org.shypl.biser.csi.client

import org.shypl.biser.csi.ConnectionCloseReason

interface ClientHandler {
	fun handleClientConnected()
	fun handleClientDisconnected(reason: ConnectionCloseReason)
	fun handleClientConnectFail(reason: Any)
	fun handleClientDisconnectWarning(seconds: Int)
}
