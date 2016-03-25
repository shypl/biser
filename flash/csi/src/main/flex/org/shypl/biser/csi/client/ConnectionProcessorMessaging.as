package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.timeline.GlobalTimeline;
	import org.shypl.common.timeline.TimeMeter;
	import org.shypl.common.util.Cancelable;

	internal class ConnectionProcessorMessaging extends ConnectionProcessor {
		private static const STATE_FLAG:int = 0;
		private static const STATE_MESSAGE_SIZE:int = 1;
		private static const STATE_MESSAGE_BODY:int = 2;

		private var _state:int = STATE_FLAG;

		private var _active:Boolean;
		private var _activeTimer:TimeMeter = new TimeMeter();
		private var _activityChecker:Cancelable;

		private var _buffer:ByteArray = new ByteArray();
		private var _messageEven:Boolean;
		private var _messageLen:int;

		override public function init(connection:Connection):void {
			super.init(connection);
			_active = true;
			_activeTimer.start();
			_activityChecker = GlobalTimeline.scheduleRepeatable(connection.activityTimeout * 1000, checkActivity);
		}

		override public function destroy():void {
			super.destroy();
			_activityChecker.cancel();

			_activeTimer = null;
			_activityChecker = null;
		}

		override public function processClose():void {
			connection.logger.debug("Messaging: Interrupted, switch to Recovery");
			connection.setProcessor(new ConnectionProcessorRecovery());
		}

		override public function processData():void {
			_active = true;
			_activeTimer.restart();

			do {
				switch (_state) {
					case STATE_FLAG:
						readFlag();
						break;
					case STATE_MESSAGE_SIZE:
						readMessageSize();
						break;
					case STATE_MESSAGE_BODY:
						readMessageBody();
						break;
				}
			}
			while (connection.opened && connection.data.bytesAvailable > 0);
		}

		private function readFlag():void {
			var flag:int = connection.data.readUnsignedByte();
			switch (flag) {
				case Protocol.PING:
					connection.logger.trace("Pong");
					break;

				case Protocol.MESSAGE_ODD:
					prepareMessage(false);
					break;
				case Protocol.MESSAGE_EVEN:
					prepareMessage(true);
					break;

				case Protocol.MESSAGE_ODD_RECEIVED:
					connection.processMessageReceived(false);
					break;
				case Protocol.MESSAGE_EVEN_RECEIVED:
					connection.processMessageReceived(true);
					break;

				default:
					closeConnection(flag);
					break;
			}
		}

		private function prepareMessage(even:Boolean):void {
			_state = STATE_MESSAGE_SIZE;
			_messageEven = even;
		}

		private function readMessageSize():void {
			var i:uint = _buffer.bytesAvailable;
			connection.data.readBytes(_buffer, i, Math.min(4 - i, connection.data.bytesAvailable));

			if (4 == _buffer.bytesAvailable) {
				_state = STATE_MESSAGE_BODY;
				_messageLen = _buffer.readInt();
				_buffer.clear();
			}
		}

		private function readMessageBody():void {
			var i:uint = _buffer.bytesAvailable;
			connection.data.readBytes(_buffer, i, Math.min(_messageLen - i, connection.data.bytesAvailable));

			if (_buffer.bytesAvailable == _messageLen) {
				_state = STATE_FLAG;

				var message:ByteArray = new ByteArray();
				_buffer.readBytes(message);
				_buffer.clear();

				connection.receiveMessage(_messageEven, message);
			}
		}


		private function checkActivity():void {
			if (_active) {
				if (_activeTimer.seconds >= connection.activityTimeout) {
					_active = false;
					connection.logger.trace("Ping");
					connection.sendByte(Protocol.PING);
				}
			}
			else {
				_activityChecker.cancel();
				connection.logger.warn("Messaging: Connection is not active, interrupt");
				connection.interrupt();
			}
		}
	}
}
