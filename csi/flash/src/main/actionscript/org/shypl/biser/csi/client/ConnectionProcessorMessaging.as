package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;
	
	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.timeline.GlobalTimeline;
	import org.shypl.common.util.Cancelable;
	import org.shypl.common.util.TimeMeter;
	
	internal class ConnectionProcessorMessaging extends ConnectionProcessor {
		private static const STATE_MESSAGE_ID:int = 0;
		private static const STATE_MESSAGE_SIZE:int = 1;
		private static const STATE_MESSAGE_BODY:int = 2;
		
		private var _activity:Boolean;
		private var _activityTimer:TimeMeter = new TimeMeter();
		private var _activityTimeout:int;
		private var _activityChecker:Cancelable;
		
		private var _state:int;
		private var _messageId:int;
		
		public function ConnectionProcessorMessaging() {
		}
		
		override public function init(connection:Connection):void {
			super.init(connection);
			_activity = true;
			_activityTimer.start();
			
			_activityTimeout = connection.activityTimeout / 2;
			_activityChecker = GlobalTimeline.scheduleRepeatable(_activityTimeout * 1000 / 2, checkActivity);
		}
		
		override public function destroy():void {
			super.destroy();
			_activityChecker.cancel();
			_activityChecker = null;
		}
		
		override public function processClose():void {
			if (connection.recoveryTimeout <= 0) {
				connection.logger.debug("Messaging: Interrupted, close");
				connection.close(ConnectionCloseReason.RECOVERY_REJECT);
			}
			else {
				connection.logger.debug("Messaging: Interrupted, switch to Recovery");
				connection.setProcessor(new ConnectionProcessorRecovery());
			}
		}
		
		override public function processData(data:IDataInput):void {
			_activity = true;
			_activityTimer.restart();
			do {
				super.processData(data);
			}
			while (isCanReadData(data));
		}
		
		override protected function processDataFlag(flag:uint):void {
			switch (flag) {
				case Protocol.PING:
					break;
				
				case Protocol.MESSAGE:
					prepareMessage();
					break;
				
				case Protocol.MESSAGE_RECEIVED:
					connection.processOutgoingMessageReceived();
					break;
				
				default:
					super.processDataFlag(flag);
					break;
			}
			
		}
		
		override protected function processDataBody(buffer:ByteArray):void {
			switch (_state) {
				case STATE_MESSAGE_ID:
					_messageId = buffer.readInt();
					_state = STATE_MESSAGE_SIZE;
					setDataExpectBody(4);
					break;
				case STATE_MESSAGE_SIZE:
					_state = STATE_MESSAGE_BODY;
					setDataExpectBody(buffer.readInt());
					break;
				case STATE_MESSAGE_BODY:
					var data:ByteArray = new ByteArray();
					buffer.readBytes(data);
					connection.receiveMessage(_messageId, data);
					break;
			}
		}
		
		private function prepareMessage():void {
			_state = STATE_MESSAGE_ID;
			setDataExpectBody(4);
		}
		
		private function checkActivity():void {
			if (_activity) {
				if (_activityTimer.seconds >= _activityTimeout) {
					_activity = false;
					connection.sendPing();
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
