package org.shypl.biser.csi.client

import org.shypl.biser.csi.ConnectionCloseReason
import org.shypl.biser.csi.Protocol
import org.shypl.biser.io.ByteArrayBuffer
import org.shypl.biser.io.InputData
import ru.capjack.ktjs.common.invokeDelayed
import kotlin.math.min

internal abstract class ConnectionProcessor {
	lateinit var connection: Connection
		private set
	
	private var dataExpectFlag = true
	private var dataExpectedSize = 0
	private var dataBuffer = ByteArrayBuffer()
	
	private var waitDataForServerShutdownTimeout = false
	
	open fun init(connection: Connection) {
		this.connection = connection
	}
	
	open fun destroy() {
		dataBuffer.clear()
	}
	
	open fun processAccept() {
		throw UnsupportedOperationException()
	}
	
	open fun processData(data: InputData) {
		if (dataExpectFlag) {
			processDataFlag(data.readByte())
		} else {
			readDataBody(data)
		}
	}
	
	open fun processClose() {}
	
	protected fun isCanReadData(data: InputData): Boolean {
		return connection.opened && data.isReadable()
	}
	
	protected fun setDataExpectFlag() {
		dataExpectFlag = true
	}
	
	protected fun setDataExpectBody(size: Int) {
		dataExpectFlag = false
		dataExpectedSize = size
		dataBuffer.clear()
	}
	
	protected open fun processDataFlag(flag: Byte) {
		if (flag == Protocol.SERVER_SHUTDOWN_TIMEOUT) {
			waitDataForServerShutdownTimeout = true
			setDataExpectBody(4)
		} else {
			closeConnection(flag)
		}
	}
	
	protected open fun processDataBody(data: InputData) {
		throw UnsupportedOperationException()
	}
	
	private fun readDataBody(data: InputData) {
		val bufferSize = dataBuffer.readableBytes
		data.readBytes(dataBuffer, min(dataExpectedSize - bufferSize, data.readableBytes))
		
		if (dataExpectedSize == dataBuffer.readableBytes) {
			setDataExpectFlag()
			processDataBodyInternal()
		}
	}
	
	private fun processDataBodyInternal() {
		if (waitDataForServerShutdownTimeout) {
			waitDataForServerShutdownTimeout = false
			invokeDelayed { connection.client.processDisconnectWarning(dataBuffer.readInt()) }
		} else {
			processDataBody(dataBuffer)
		}
	}
	
	private fun closeConnection(flag: Byte) {
		var reason = ConnectionCloseReason.getReason(flag)
		if (reason == null) {
			connection.logger.error("ConnectionProcessor: Undefined flag {}", flag)
			reason = ConnectionCloseReason.PROTOCOL_BROKEN
		}
		connection.close(reason)
	}
}
