package org.shypl.biser.csi.client

import org.shypl.biser.csi.CommunicationLoggingUtils
import org.shypl.biser.io.DataReader
import org.shypl.biser.io.InputData
import org.shypl.biser.io.OutputData
import ru.capjack.ktjs.common.logging.Logger
import ru.capjack.ktjs.common.logging.Logging

abstract class AbstractApi {
	val logger: Logger = Logging.get("CSI")
	
	internal var connection: Connection? = null
	
	private var resultHandlers = mutableMapOf<Int, ResultHandlerHolder>()
	private var resultHandlerIdCounter = 0
	
	protected abstract fun callService(serviceId: Int, actionId: Int, reader: DataReader)
	
	protected fun logCall(serviceName: String, methodName: String, vararg args: Any?) {
		CommunicationLoggingUtils.logServerCall(logger, serviceName, methodName, args)
	}
	
	internal fun processIncomingMessage(message: InputData) {
		val reader: DataReader = DataReader(message)
		val resultHandlerId: Int = reader.readInt()
		if (resultHandlerId != 0) {
			val resultHandler: ResultHandlerHolder = resultHandlers.getValue(resultHandlerId)
			resultHandlers.remove(resultHandlerId)
			resultHandler.process0(reader, logger)
		} else {
			callService(reader.readInt(), reader.readInt(), reader)
		}
	}
	
	internal fun sendMessage(data: OutputData) {
		if (connection === null) {
			throw RuntimeException("Api is not connected")
		} else {
			connection?.sendMessage(data)
		}
	}
	
	internal fun registerResultHandler(holder: ResultHandlerHolder): Int {
		++resultHandlerIdCounter
		if (resultHandlerIdCounter == 0) {
			++resultHandlerIdCounter
		}
		resultHandlers[resultHandlerIdCounter] = holder
		return resultHandlerIdCounter
	}
}