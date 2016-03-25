package org.shypl.biser.csi {
	import org.shypl.common.logging.Logger;
	import org.shypl.common.util.StringUtils;

	public class CommunicationLoggingUtils {
		public static function logServerCall(logger:Logger, serviceName:String, methodName:String, args:Array):void {
			logCall(false, logger, serviceName, methodName, args);
		}

		public static function logServerResponse(logger:Logger, serviceName:String, methodName:String, result:Object):void {
			if (logger.isTraceEnabled()) {
				logger.trace(createMessage(false, serviceName, methodName) + ": " + StringUtils.toString(result));
			}
		}

		public static function logClientCall(logger:Logger, serviceName:String, methodName:String, args:Array):void {
			logCall(true, logger, serviceName, methodName, args);
		}

		private static function logCall(cts:Boolean, logger:Logger, serviceName:String, methodName:String, args:Array):void {
			if (logger.isTraceEnabled()) {
				var message:String = createMessage(cts, serviceName, methodName);

				message += "(";
				var sep:Boolean = false;
				for each (var arg:Object in args) {
					if (sep) {
						message += ", ";
					}
					else {
						sep = true;
					}
					message += StringUtils.toString(arg);
				}
				message += ")";

				logger.trace(message);
			}
		}

		private static function createMessage(cts:Boolean, serviceName:String, methodName:String):String {
			return (cts ? "> " : "< ") + serviceName + "." + methodName;
		}


	}
}
