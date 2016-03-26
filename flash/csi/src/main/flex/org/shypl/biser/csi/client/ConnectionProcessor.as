package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.lang.UnsupportedOperationException;
	import org.shypl.common.util.callDelayed;

	[Abstract]
	internal class ConnectionProcessor {

		private var _connection:Connection;

		private var _dataExpectFlag:Boolean = true;
		private var _dataExpectedSize:int;
		private var _dataBuffer:ByteArray = new ByteArray();

		private var _waitDataForServerShutdownTimeout:Boolean;

		public function ConnectionProcessor() {
		}

		protected final function get connection():Connection {
			return _connection;
		}

		public function init(connection:Connection):void {
			_connection = connection;
		}

		public function destroy():void {
			_dataBuffer.clear();
			_connection = null;
			_dataBuffer = null;
		}

		public function processAccept():void {
			throw new UnsupportedOperationException();
		}

		public function processData(data:IDataInput):void {
			if (_dataExpectFlag) {
				processDataFlag(data.readUnsignedByte());
			}
			else {
				readDataBody(data);
			}
		}

		public function processClose():void {
		}

		protected function isCanReadData(data:IDataInput):Boolean {
			return _connection.opened && data.bytesAvailable > 0;
		}

		protected final function setDataExpectFlag():void {
			_dataExpectFlag = true;
		}

		protected final function setDataExpectBody(size:int):void {
			_dataExpectFlag = false;
			_dataExpectedSize = size;
			_dataBuffer.clear();
		}

		protected function processDataFlag(flag:uint):void {
			if (flag === Protocol.SERVER_SHUTDOWN_TIMEOUT) {
				_waitDataForServerShutdownTimeout = true;
				setDataExpectBody(4);
			}
			else {
				closeConnection(flag);
			}
		}

		protected function processDataBody(buffer:ByteArray):void {
			throw new UnsupportedOperationException();
		}

		private function readDataBody(data:IDataInput):void {
			var bufferSize:uint = _dataBuffer.bytesAvailable;
			data.readBytes(_dataBuffer, bufferSize, _dataExpectedSize - bufferSize);

			if (_dataExpectedSize == _dataBuffer.bytesAvailable) {
				setDataExpectFlag();
				processDataBodyInternal(_dataBuffer);
			}
		}

		private function processDataBodyInternal(buffer:ByteArray):void {
			if (_waitDataForServerShutdownTimeout) {
				callDelayed(_connection.client.processDisconnectWarning, buffer.readInt());
			}
			else {
				processDataBody(buffer);
			}
		}

		private function closeConnection(flag:uint):void {
			var reason:ConnectionCloseReason = ConnectionCloseReason.getReason(flag);
			if (reason == null) {
				_connection.logger.error("ConnectionProcessor: Undefined flag {}", flag);
				reason = ConnectionCloseReason.PROTOCOL_BROKEN;
			}
			_connection.close(reason);
		}
	}
}
