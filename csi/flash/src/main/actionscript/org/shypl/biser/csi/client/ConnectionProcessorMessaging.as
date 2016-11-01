package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;
	
	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.timeline.GlobalTimeline;
	import org.shypl.common.util.Cancelable;
	import org.shypl.common.util.TimeMeter;
	
	internal class ConnectionProcessorMessaging extends ConnectionProcessor {
		private var _activity:Boolean;
		private var _activityTimer:TimeMeter = new TimeMeter();
		private var _activityTimeout:int;
		private var _activityChecker:Cancelable;
		
		private var _messageSizeMode:Boolean;
		
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
				
				default:
					super.processDataFlag(flag);
					break;
			}
			
		}
		
		override protected function processDataBody(buffer:ByteArray):void {
			if (_messageSizeMode) {
				_messageSizeMode = false;
				setDataExpectBody(buffer.readInt());
			}
			else {
				var message:ByteArray = new ByteArray();
				buffer.readBytes(message);
				connection.receiveMessage(message);
			}
		}
		
		private function prepareMessage():void {
			_messageSizeMode = true;
			setDataExpectBody(4);
		}
		
		private function checkActivity():void {
			if (_activity) {
				if (_activityTimer.seconds >= _activityTimeout) {
					_activity = false;
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
