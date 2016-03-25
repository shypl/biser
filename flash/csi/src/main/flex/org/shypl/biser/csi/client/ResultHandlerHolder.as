package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.CommunicationLoggingUtils;
	import org.shypl.biser.io.DataReader;
	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.logging.Logger;

	[Abstract]
	public class ResultHandlerHolder {
		private var _logger:Logger;

		[Abstract]
		protected function process(reader:DataReader):void {
			throw new AbstractMethodException();
		}

		internal function process0(reader:DataReader, logger:Logger):void {
			_logger = logger;
			process(reader);
			_logger = null;
		}

		protected final function log(serviceName:String, methodName:String, result:Object):void {
			CommunicationLoggingUtils.logServerResponse(_logger, serviceName, methodName, result);
		}
	}
}
