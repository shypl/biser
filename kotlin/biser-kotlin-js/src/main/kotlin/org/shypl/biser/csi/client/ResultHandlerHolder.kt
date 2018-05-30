package org.shypl.biser.csi.client

import org.shypl.biser.csi.CommunicationLoggingUtils
import org.shypl.biser.io.DataReader
import ru.capjack.ktjs.common.logging.Logger

abstract class ResultHandlerHolder {
	private var logger: Logger? = null
	
	protected fun log(serviceName: String, methodName: String, result: Any) {
		CommunicationLoggingUtils.logServerResponse(logger!!, serviceName, methodName, result);
	}
	
	protected abstract fun process(reader: DataReader)
	
	fun process0(reader: DataReader, logger: Logger) {
		this.logger = logger
		process(reader)
		this.logger = null
	}
}
