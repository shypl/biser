package org.shypl.biser.api.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.api.ApiException;
	import org.shypl.biser.api.Protocol;

	public class ConnectionStrategyConnect extends ConnectionStrategy {
		private static const SID_LENGTH:int = 8 + 16;

		private var _connectHandler:ConnectHandler;
		private var _stateFlag:Boolean = true;
		private var _sid:ByteArray;

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
			throw new ApiException("Unexpected connection termination");
		}

		override internal function destroy():void {
			super.destroy();
			_connectHandler = null;
			_sid = null;
		}

		private function readFlag(data:IDataInput):void {
			var flag:int = data.readByte();
			if (flag == Protocol.CONNECT_SUCCESS) {
				_sid = new ByteArray();
			}
			else {
				var reason:ConnectionCloseReason = ConnectionCloseReason.defineByFlag(flag);
				getLogger().info("Authorization fail, reason {}", reason);
				var connectHandler:ConnectHandler = _connectHandler;
				_connection.doClose(reason);
				if (connectHandler) {
					connectHandler.handlerConnectFail(reason);
				}
			}
		}

		private function readSid(data:IDataInput):void {
			var offset:uint = _sid.bytesAvailable;
			data.readBytes(_sid, offset, Math.min(SID_LENGTH - offset, data.bytesAvailable));

			if (_sid.bytesAvailable == SID_LENGTH) {
				var connectHandler:ConnectHandler = _connectHandler;
				_connection.authorize(_sid);
				if (connectHandler) {
					connectHandler.handlerConnectSuccess();
				}
			}
		}
	}
}
