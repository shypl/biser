package org.shypl.biser.csi.client

import org.shypl.biser.csi.ConnectionCloseReason
import org.shypl.biser.csi.Protocol
import org.shypl.biser.io.ByteArrayOutputData
import org.shypl.biser.io.InputData
import org.shypl.biser.io.PlatformUtils.encodeStringUtf8
import org.shypl.biser.io.toUint

internal open class ConnectionProcessorAuthorization(
	private val authorizationKey: String
) : ConnectionProcessor() {
	/**
	 * ---
	 * activityTimeout int
	 * recoveryTimeout int
	 * sid.length byte
	 * ---
	 * sid bytes
	 */
	private val HEADER_LENGTH = 4 + 4 + 1
	
	private var _headerMode = false
	
	private var _activityTimeout = 0
	private var _recoveryTimeout = 0
	
	override fun processAccept() {
		connection.logger.debug("Authorization: Authorize by key {}", authorizationKey)
		
		val key: ByteArray = encodeStringUtf8(authorizationKey)
		val data = ByteArrayOutputData()
		data.writeByte(Protocol.AUTHORIZATION)
		data.writeByte(key.size.toByte())
		data.writeBytes(key)
		
		connection.sendBytes(data.toByteArray())
	}
	
	override fun processClose() {
		connection.logger.debug("Authorization: Broken")
		connection.close(ConnectionCloseReason.PROTOCOL_BROKEN)
	}
	
	override fun processDataFlag(flag: Byte) {
		if (flag == Protocol.AUTHORIZATION) {
			connection.logger.debug("Authorization: Success")
			_headerMode = true
			setDataExpectBody(HEADER_LENGTH)
		} else {
			connection.logger.debug("Authorization: Fail")
			super.processDataFlag(flag)
		}
	}
	
	override fun processDataBody(data: InputData) {
		if (_headerMode) {
			_headerMode = false
			_activityTimeout = data.readInt()
			_recoveryTimeout = data.readInt()
			setDataExpectBody(data.readByte().toUint())
		} else {
			val sid = ByteArray(data.readableBytes)
			data.readBytes(sid)
			
			connection.beginSession(sid, _activityTimeout, _recoveryTimeout)
			
			connection.logger.debug("Authorization: Switch to Messaging")
			connection.setProcessor(ConnectionProcessorMessaging())
		}
	}
}
