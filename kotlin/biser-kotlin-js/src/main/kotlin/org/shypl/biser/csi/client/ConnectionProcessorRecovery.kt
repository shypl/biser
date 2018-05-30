package org.shypl.biser.csi.client

import org.shypl.biser.csi.ConnectionCloseReason
import org.shypl.biser.csi.Protocol
import org.shypl.biser.io.ByteArrayOutputData
import org.shypl.biser.io.InputData
import org.shypl.biser.io.toUint
import kotlin.browser.window

internal open class ConnectionProcessorRecovery : ConnectionProcessor() {
	private var _timeout = 0
	private var _delayedConnect = 0
	
	private var _headerMode = false
	
	override fun init(connection: Connection) {
		super.init(connection)
		_timeout = connection.recoveryTimeout
		scheduleConnect()
	}
	
	override fun destroy() {
		super.destroy()
		window.clearTimeout(_delayedConnect)
	}
	
	override fun processAccept() {
		window.clearTimeout(_delayedConnect)
		
		val sid: ByteArray = connection.sid!!
		
		connection.logger.debug("Recovery: Connect succeeded, authorize by sid {}", sid)
		
		val data = ByteArrayOutputData()
		data.writeByte(Protocol.RECOVERY)
		data.writeByte((4 + sid.size).toByte())
		data.writeInt(connection.lastIncomingMessageId)
		data.writeBytes(sid)
		
		connection.sendBytes(data.toByteArray())
	}
	
	override fun processClose() {
		connection.close(ConnectionCloseReason.RECOVERY_TIMEOUT_EXPIRED)
	}
	
	override fun processDataFlag(flag: Byte) {
		if (flag == Protocol.RECOVERY) {
			connection.logger.debug("Recovery: Success")
			
			_headerMode = true
			setDataExpectBody(1)
		} else {
			connection.logger.debug("Recovery: Fail")
			super.processDataFlag(flag)
		}
	}
	
	override protected fun processDataBody(data: InputData) {
		if (_headerMode) {
			_headerMode = false
			setDataExpectBody(data.readByte().toUint())
		} else {
			val messageId = data.readInt()
			val sid = ByteArray(data.readableBytes)
			data.readBytes(sid)
			connection.recoverSession(sid, messageId)
			
			connection.logger.debug("Recovery: Switch to Messaging")
			connection.setProcessor(ConnectionProcessorMessaging())
		}
	}
	
	private fun scheduleConnect() {
		val ms = 1000
		
		connection.logger.debug("Recovery: Schedule connect after {} ms", ms)
		
		_delayedConnect = window.setTimeout(::connect, ms)
	}
	
	private fun connect() {
		connection.logger.debug("Recovery: Attempt to connect ({})", 1)
		connection.openChannel()
	}
}
