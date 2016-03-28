package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;

	internal class ConnectionProcessorAuthorization extends ConnectionProcessor {
		/**
		 * ---
		 * activityTimeout int
		 * recoveryTimeout int
		 * sid.length byte
		 * ---
		 * sid bytes
		 */
		private static const HEADER_LENGTH:int = 4 + 4 + 1;

		private var _authorizationKey:String;

		private var _headerMode:Boolean;

		private var _activityTimeout:int;
		private var _recoveryTimeout:int;

		public function ConnectionProcessorAuthorization(authorizationKey:String) {
			_authorizationKey = authorizationKey;
		}

		override public function destroy():void {
			super.destroy();
			_authorizationKey = null;
		}

		override public function processAccept():void {
			var key:ByteArray = new ByteArray();
			key.writeUTFBytes(_authorizationKey);

			connection.logger.debug("Authorization: Authorize by key {}", _authorizationKey);

			var data:ByteArray = new ByteArray();
			data.writeByte(Protocol.AUTHORIZATION);
			data.writeByte(key.length);
			data.writeBytes(key);

			connection.sendBytes(data);
		}

		override public function processClose():void {
			connection.logger.debug("Authorization: Broken");
			connection.close(ConnectionCloseReason.PROTOCOL_BROKEN);
		}

		override protected function processDataFlag(flag:uint):void {
			if (flag === Protocol.AUTHORIZATION) {
				connection.logger.debug("Authorization: Success");
				_headerMode = true;
				setDataExpectBody(HEADER_LENGTH);
			}
			else {
				connection.logger.debug("Authorization: Fail");
				super.processDataFlag(flag);
			}
		}

		[Abstract]
		override protected function processDataBody(buffer:ByteArray):void {
			if (_headerMode) {
				_headerMode = false;
				_activityTimeout = buffer.readInt();
				_recoveryTimeout = buffer.readInt();
				setDataExpectBody(buffer.readUnsignedByte());
			}
			else {
				var sid:ByteArray = new ByteArray();
				buffer.readBytes(sid);

				connection.beginSession(sid, _activityTimeout, _recoveryTimeout);

				connection.logger.debug("Authorization: Switch to Messaging");
				connection.setProcessor(new ConnectionProcessorMessaging());
			}
		}
	}
}
