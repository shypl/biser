package org.shypl.biser.csi.client

import org.shypl.biser.csi.ConnectionCloseReason
import ru.capjack.ktjs.common.logging.Logging

class Client(
	private val handler: ClientHandler,
	internal var channelProvider: ChannelProvider,
	internal var api: AbstractApi
) {
	val logger = Logging.get<Client>()
	
	private var connecting: Boolean = false
	private var connected: Boolean = false
	private var connection: Connection? = null
	
	fun isConnecting(): Boolean {
		return connecting
	}
	
	fun isConnected(): Boolean {
		return connected
	}
	
	fun connect(address: String, authorizationKey: String) {
		if (isConnecting() || isConnected()) {
			throw IllegalStateException("Client is already connected")
		}
		
		logger.info("Connecting to {}", address)
		connecting = true
		connection = Connection(this, address, authorizationKey)
		api.connection = connection
	}
	
	public fun disconnect() {
		if (!connecting && !connected) {
			throw IllegalStateException("Client is not connected")
		}
		connecting = false
		
		logger.info("Disconnecting")
		connection?.close(ConnectionCloseReason.NONE)
	}
	
	internal fun processConnected() {
		logger.info("Connected")
		connecting = false
		connected = true
		handler.handleClientConnected()
	}
	
	internal fun processDisconnected(reason: ConnectionCloseReason) {
		logger.info("Disconnected by reason {}", reason)
		
		connecting = false
		connected = false
		connection = null
		api.connection = null
		handler.handleClientDisconnected(reason)
	}
	
	internal fun processConnectFail(error: Throwable) {
		logger.info("Connect failed by reason {}", error)
		connecting = false
		connected = false
		handler.handleClientConnectFail(error)
	}
	
	internal fun processConnectionInterrupted() {
		logger.info("Connection is interrupted")
	}
	
	internal fun processConnectionEstablished() {
		logger.info("Connection is established")
	}
	
	internal fun processDisconnectWarning(timeout: Int) {
		logger.info("Waiting to disconnect after {} seconds", timeout)
		handler.handleClientDisconnectWarning(timeout)
	}
}