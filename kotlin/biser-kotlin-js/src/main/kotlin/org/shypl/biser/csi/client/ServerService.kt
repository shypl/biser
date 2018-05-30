package org.shypl.biser.csi.client

import org.shypl.biser.csi.CommunicationLoggingUtils
import org.shypl.biser.io.ByteArrayOutputData
import org.shypl.biser.io.DataWriter
import org.shypl.biser.io.OutputData

abstract class ServerService {
	private lateinit var _api: AbstractApi
	private var _serviceId: Int = 0
	private lateinit var _serviceName: String
	private var _message: OutputData? = null
	
	fun construct(api: AbstractApi, serviceId: Int, serviceName: String) {
		_api = api
		_serviceId = serviceId
		_serviceName = serviceName
	}
	
	protected fun _prepareMessage(methodId: Int, holder: ResultHandlerHolder? = null): DataWriter {
		_message = ByteArrayOutputData()
		val writer = DataWriter(_message!!)
		writer.writeInt(_serviceId)
		writer.writeInt(methodId)
		if (holder != null) {
			writer.writeInt(_api.registerResultHandler(holder))
		}
		return writer
	}
	
	protected fun _sendMessage() {
		_api.sendMessage(_message!!)
		_message = null
	}
	
	protected fun _log(methodName: String, vararg args: Any) {
		CommunicationLoggingUtils.logClientCall(_api.logger, _serviceName, methodName, args)
	}
}