package org.shypl.biser.csi

import org.shypl.biser.StringUtils
import ru.capjack.ktjs.common.logging.Level
import ru.capjack.ktjs.common.logging.Logger

object CommunicationLoggingUtils {
	fun logServerCall(logger: Logger, serviceName: String, methodName: String, args: Array<*>) {
		logCall(false, logger, serviceName, methodName, args)
	}
	
	fun logServerResponse(logger:Logger, serviceName: String, methodName: String, result: Any) {
		if (logger.isEnabled(Level.DEBUG)) {
			logger.debug(createMessage(false, serviceName, methodName) + ": " + StringUtils.toString(result))
		}
	}
	
	fun logClientCall(logger:Logger, serviceName: String, methodName: String, args: Array<*>) {
		logCall(true, logger, serviceName, methodName, args)
	}
	
	private fun logCall(cts: Boolean, logger:Logger, serviceName: String, methodName: String, args: Array<*>) {
		if (logger.isEnabled(Level.DEBUG)) {
			var message: String = createMessage(cts, serviceName, methodName)
			
			message += "(" + args.joinToString(", ", transform = { StringUtils.toString(it) }) + ")"
			
			logger.debug(message)
		}
	}
	
	private fun createMessage(cts: Boolean, serviceName: String, methodName: String): String {
		return (if (cts) "> " else "< ") + serviceName + "." + methodName
	}
	
}