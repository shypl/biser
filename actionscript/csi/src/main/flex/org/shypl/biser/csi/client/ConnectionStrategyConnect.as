package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.csi.CsiException;
	import org.shypl.biser.csi.Protocol;

	public class ConnectionStrategyConnect extends ConnectionStrategy {
		private static const SUCCESS_MESSAGE_LENGTH:int = 8 + 16 + 4;

		private var _connectHandler:ConnectHandler;
		private var _stateFlag:Boolean = true;
		private var _message:ByteArray;

		public function ConnectionStrategyConnect(connectHandler:ConnectHandler = null) {
			_connectHandler = connectHandler;
		}

		override public function handleData(data:IDataInput):void {
			if (_stateFlag) {
				_stateFlag = false;
				readFlag(data);
			}
			else {
				readSid(data);
			}
		}

		override public function handleClose():void {
			_connection.doClose(ConnectionCloseReason.CLIENT_ERROR);
			throw new CsiException("Unexpected connection termination");
		}

		override internal function destroy():void {
			super.destroy();
			_connectHandler = null;
			_message = null;
		}

		private function readFlag(data:IDataInput):void {
			var flag:int = data.readByte();
			if (flag == Protocol.CONNECT_SUCCESS) {
				_message = new ByteArray();
			}
			else {
				var reason:ConnectionCloseReason = ConnectionCloseReason.defineByFlag(flag);
				getLogger().info("Authorization fail, reason {}", reason);
				var connectHandler:ConnectHandler = _connectHandler;
				_connection.doClose(reason);
				if (connectHandler) {
					connectHandler.handleConnectFail(reason);
				}
			}
		}

		private function readSid(data:IDataInput):void {
			var offset:uint = _message.bytesAvailable;
			data.readBytes(_message, offset, Math.min(SUCCESS_MESSAGE_LENGTH - offset, data.bytesAvailable));

			if (_message.bytesAvailable == SUCCESS_MESSAGE_LENGTH) {
				var connectHandler:ConnectHandler = _connectHandler;
				_connection.authorize(_message);
				if (connectHandler) {
					connectHandler.handleConnectSuccess();
				}
			}
		}
	}
}
