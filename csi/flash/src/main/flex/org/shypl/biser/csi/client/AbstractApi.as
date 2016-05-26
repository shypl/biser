package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.csi.CommunicationLoggingUtils;
	import org.shypl.biser.io.DataReader;
	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.Logger;

	[Abstract]
	public class AbstractApi {
		private var _name:String;
		private var _resultHandlers:Object = {};
		private var _resultHandlerIdCounter:uint;
		private var _connection:Connection;

		public function AbstractApi(name:String) {
			_name = name;
		}

		public final function get name():String {
			return _name;
		}

		public function get logger():Logger {
			if (_connection === null) {
				throw new IllegalStateException("Api is not connected");
			}
			return _connection.logger;
		}

		[Abstract]
		protected function callService(serviceId:int, methodId:int, reader:DataReader):void {
			throw new AbstractMethodException();
		}

		protected final function logCall(serviceName:String, methodName:String, ...args):void {
			CommunicationLoggingUtils.logServerCall(logger, serviceName, methodName, args);
		}

		internal function setConnection(connection:Connection):void {
			_connection = connection;
		}

		internal function processIncomingMessage(message:IDataInput):void {
			var reader:DataReader = new DataReader(message);
			var resultHandlerId:uint = reader.readUint();
			if (resultHandlerId != 0) {
				var resultHandler:ResultHandlerHolder = _resultHandlers[resultHandlerId];
				delete _resultHandlers[resultHandlerId];
				resultHandler.process0(reader, logger);
			}
			else {
				callService(reader.readInt(), reader.readInt(), reader);
			}
		}

		internal function sendMessage(data:ByteArray):void {
			if (_connection === null) {
				throw new IllegalStateException("Api is not connected");
			}
			_connection.sendMessage(data);
		}

		internal function registerResultHandler(holder:ResultHandlerHolder):int {
			++_resultHandlerIdCounter;
			_resultHandlers[_resultHandlerIdCounter] = holder;
			return _resultHandlerIdCounter;
		}
	}
}
