package test.app

import org.shypl.biser.csi.ConnectionCloseReason
import org.shypl.biser.csi.client.Client
import org.shypl.biser.csi.client.ClientHandler
import org.shypl.biser.csi.client.WebSocketChannelProvider
import test.csi.SessionUser
import test.csi.api.Api
import test.csi.api.SessionGetUserResultHandler

fun main(args: Array<String>) {
	val api = Api()
	val client = Client(TestClientHandler(api), WebSocketChannelProvider(), api)
	client.connect("ws://localhost:8888", "GOD")
}

class TestClientHandler(
	private val api: Api

) : ClientHandler {
	
	override fun handleClientConnected() {
		console.log("handleClientConnected")
		
		api.server.session.getUser(object : SessionGetUserResultHandler {
			override fun handleResultSessionGetUser(result: SessionUser) {
				console.log("handleResultSessionGetUser", result)
				console.log(result.balance.toString())
			}
		})
	}
	
	override fun handleClientDisconnected(reason: ConnectionCloseReason) {
		console.log("handleClientDisconnected", reason)
	}
	
	override fun handleClientConnectFail(reason: Any) {
		console.log("handleClientConnectFail", reason)
	}
	
	override fun handleClientDisconnectWarning(seconds: Int) {
		console.log("handleClientDisconnectWarning", seconds)
	}
}