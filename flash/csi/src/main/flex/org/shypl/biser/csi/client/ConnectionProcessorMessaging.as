package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.timeline.GlobalTimeline;
	import org.shypl.common.util.Cancelable;
	import org.shypl.common.util.TimeMeter;

	internal class ConnectionProcessorMessaging extends ConnectionProcessor {
		private var _active:Boolean;
		private var _activeTimer:TimeMeter = new TimeMeter();
		private var _activityChecker:Cancelable;

		private var _messageEven:Boolean;
		private var _messageSizeMode:Boolean;

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

		override public function processData(data:IDataInput):void {
			_active = true;
			_activeTimer.restart();
			do {
				super.processData(data);
			}
			while (connection.opened && data.bytesAvailable > 0);
		}

		override protected function processDataFlag(flag:uint):void {
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
					super.processDataFlag(flag);
					break;
			}

		}

		override protected function processDataBody(buffer:ByteArray):void {
			if (_messageSizeMode = true) {
				_messageSizeMode = false;
				setDataExpectBody(buffer.readInt());
			}
			else {
				var message:ByteArray = new ByteArray();
				buffer.readBytes(message);
				connection.receiveMessage(_messageEven, message);
			}
		}

		private function prepareMessage(even:Boolean):void {
			_messageEven = even;
			_messageSizeMode = true;
			setDataExpectBody(4);
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
