package org.shypl.biser.api.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.io.BiserReader;
	import org.shypl.biser.io.StreamReader;
	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.Level;

	[Abstract]
	public class AbstractApiGate {
		private var _connection:Connection;
		private var _resultHandlers:Object = {};
		private var _resultHandlerCounter:int;

		internal function setConnection(connection:Connection):void {
			if (_connection) {
				throw new IllegalStateException();
			}
			_connection = connection;
		}

		internal function removeConnection():void {
			_connection = null;
		}

		internal function processMessage(data:IDataInput):void {
			var reader:BiserReader = new StreamReader(data);

			var resultId:int = reader.readInt();
			if (resultId != 0) {
				var resultHandler:ResultHandlerHolder = _resultHandlers[resultId];
				delete _resultHandlers[resultId];
				resultHandler.process(reader, _connection.getLogger());
			}
			else {
				execute(reader.readInt(), reader.readInt(), reader);
			}
		}

		internal function sendMessage(data:ByteArray):void {
			_connection.sendMessage(data);
		}

		internal function registerResultHandler(holder:ResultHandlerHolder):int {
			if (++_resultHandlerCounter == 0) {
				++_resultHandlerCounter;
			}
			_resultHandlers[_resultHandlerCounter] = holder;
			return _resultHandlerCounter;
		}

		internal function _log(message:String, ...args):void {
			_connection.getLogger().log(Level.TRACE, message, args);
		}

		protected final function log(message:String, ...args):void {
			_connection.getLogger().log(Level.TRACE, message, args);
		}

		[Abstract]
		protected function execute(serviceId:int, actionId:int, reader:BiserReader):void {
			throw new AbstractMethodException();
		}
	}
}
