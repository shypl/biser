package org.shypl.biser.csi.client

import org.shypl.biser.csi.ConnectionCloseReason
import org.shypl.biser.csi.Protocol
import org.shypl.biser.io.ByteArrayInputData
import org.shypl.biser.io.InputData
import kotlin.browser.window

internal class ConnectionProcessorMessaging : ConnectionProcessor() {
	private val STATE_MESSAGE_ID = 0
	private val STATE_MESSAGE_SIZE = 1
	private val STATE_MESSAGE_BODY = 2
	private val STATE_OUTGOING_MESSAGE_RECEIVED = 3
	
	private var activity = false
	private var activityChecker = 0
	
	private var state = STATE_MESSAGE_ID
	private var messageId = 0
	
	override fun init(connection: Connection) {
		super.init(connection)
		activity = true
		activityChecker = window.setInterval(::checkActivity, connection.activityTimeout * 1000 / 2)
	}
	
	override fun destroy() {
		super.destroy()
		window.clearInterval(activityChecker)
	}
	
	override fun processClose() {
		if (connection.recoveryTimeout <= 0) {
			connection.logger.debug("Messaging: Interrupted, close")
			connection.close(ConnectionCloseReason.RECOVERY_REJECT)
		} else {
			connection.logger.debug("Messaging: Interrupted, switch to Recovery")
			connection.setProcessor(ConnectionProcessorRecovery())
		}
	}
	
	override fun processData(data: InputData) {
		activity = true
		do {
			super.processData(data)
		} while (isCanReadData(data))
	}
	
	override fun processDataFlag(flag: Byte) {
		when (flag) {
			Protocol.PING             -> Unit
			Protocol.MESSAGE          -> prepareMessage()
			Protocol.MESSAGE_RECEIVED -> prepareOutgoingMessageReceived()
			else                      -> super.processDataFlag(flag)
		}
		
	}
	
	override fun processDataBody(data: InputData) {
		when(state) {
			STATE_MESSAGE_ID -> {
				messageId = data.readInt()
				state = STATE_MESSAGE_SIZE
				setDataExpectBody(4)
			}
			STATE_MESSAGE_SIZE -> {
				state = STATE_MESSAGE_BODY
				setDataExpectBody(data.readInt())
			}
			STATE_MESSAGE_BODY -> {
				val message: ByteArray = ByteArray(data.readableBytes)
				data.readBytes(message)
				connection.receiveMessage(messageId, ByteArrayInputData(message))
			}
			STATE_OUTGOING_MESSAGE_RECEIVED -> {
				connection.processOutgoingMessageReceived(data.readInt())
			}
		}
	}
	
	private fun prepareOutgoingMessageReceived() {
		state = STATE_OUTGOING_MESSAGE_RECEIVED
		setDataExpectBody(4)
	}
	
	private fun prepareMessage() {
		state = STATE_MESSAGE_ID
		setDataExpectBody(4)
	}
	
	private fun checkActivity() {
		if (activity) {
			activity = false
			connection.sendPing()
		} else {
			window.clearInterval(activityChecker)
			connection.logger.warn("Messaging: Connection is not active, interrupt")
			connection.interrupt()
		}
	}
}
