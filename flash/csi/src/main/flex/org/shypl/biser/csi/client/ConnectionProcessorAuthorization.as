package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.math.Long;

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

		private static const STATE_FLAG:int = 0;
		private static const STATE_HEADER:int = 0;
		private static const STATE_SID:int = 0;

		private var _authorizationKey:String;

		private var _state:int = STATE_FLAG;
		private var _buffer:ByteArray = new ByteArray();

		private var _activityTimeout:int;
		private var _recoveryTimeout:int;
		private var _sidLength:uint;

		public function ConnectionProcessorAuthorization(authorizationKey:String) {
			_authorizationKey = authorizationKey;
		}

		override public function destroy():void {
			super.destroy();
			_buffer.clear();

			_authorizationKey = null;
			_buffer = null;
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

		override public function processData():void {
			switch (_state) {
				case STATE_FLAG:
					readFlag();
					break;
				case STATE_HEADER:
					readHeader();
					break;
				case STATE_SID:
					readSid();
					break;
			}
		}

		override public function processClose():void {
			connection.logger.debug("Authorization: Broken");
			connection.close(ConnectionCloseReason.PROTOCOL_BROKEN);
		}

		private function readFlag():void {
			var flag:int = connection.data.readUnsignedByte();
			if (flag == Protocol.AUTHORIZATION) {
				connection.logger.debug("Authorization: Success");
				_state = STATE_HEADER;
			}
			else {
				connection.logger.debug("Authorization: Fail");
				closeConnection(flag);
			}
		}

		private function readHeader():void {
			var offset:uint = _buffer.bytesAvailable;
			connection.data.readBytes(_buffer, offset, Math.min(HEADER_LENGTH - offset, connection.data.bytesAvailable));

			if (_buffer.bytesAvailable == HEADER_LENGTH) {

				_activityTimeout = _buffer.readInt();
				_recoveryTimeout = _buffer.readInt();
				_sidLength = _buffer.readUnsignedByte();

				_buffer.clear();
				_state = STATE_SID;
			}
		}

		private function readSid():void {
			var offset:uint = _buffer.bytesAvailable;
			connection.data.readBytes(_buffer, offset, Math.min(_sidLength - offset, connection.data.bytesAvailable));

			if (_buffer.bytesAvailable == _sidLength) {
				var sid:ByteArray = new ByteArray();
				_buffer.readBytes(sid);

				connection.beginSession(sid, _activityTimeout, _recoveryTimeout);

				connection.logger.debug("Authorization: Switch to Messaging");
				connection.setProcessor(new ConnectionProcessorMessaging());
			}
		}
	}
}
