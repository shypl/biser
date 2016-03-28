package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.timeline.GlobalTimeline;
	import org.shypl.common.util.Cancelable;
	import org.shypl.common.util.TimeMeter;

	internal class ConnectionProcessorRecovery extends ConnectionProcessor {
		private var _timer:TimeMeter = new TimeMeter();
		private var _timeout:int;
		private var _attempt:int;
		private var _delayedConnect:Cancelable;

		public function ConnectionProcessorRecovery() {
		}

		override public function init(connection:Connection):void {
			super.init(connection);
			_timeout = connection.activityTimeout * 2 + connection.recoveryTimeout;
			_timer.start();
			scheduleConnect();
		}

		override public function destroy():void {
			super.destroy();
			_delayedConnect.cancel();
			_timer = null;
			_delayedConnect = null;
		}

		override public function processAccept():void {
			_delayedConnect.cancel();

			var sid:ByteArray = connection.sid;

			connection.logger.debug("Recovery: Connect succeeded, authorize by sid {}", sid);

			var data:ByteArray = new ByteArray();
			data.writeByte(Protocol.RECOVERY);
			data.writeByte(sid.length);
			data.writeBytes(sid);

			connection.sendBytes(data);
		}

		override public function processClose():void {
			if (_timer.seconds >= _timeout) {
				_delayedConnect.cancel();
				connection.logger.debug("Recovery: Timeout expired (gone {} ms)", _timer.seconds);
				connection.close(ConnectionCloseReason.RECOVERY_TIMEOUT_EXPIRED);
			}
			else {
				scheduleConnect();
			}
		}

		override protected function processDataFlag(flag:uint):void {
			if (flag == Protocol.RECOVERY) {
				connection.logger.debug("Recovery: Success");
				connection.recoverSession();

				connection.logger.debug("Recovery: Switch to Messaging");
				connection.setProcessor(new ConnectionProcessorMessaging());
			}
			else {
				connection.logger.debug("Recovery: Fail");
				super.processDataFlag(flag);
			}
		}

		private function scheduleConnect():void {
			var ms:int = _attempt == 0 ? 100 : 1000;

			connection.logger.debug("Recovery: Schedule connect after {} ms", ms);

			_delayedConnect = GlobalTimeline.schedule(ms, connect);
		}

		private function connect():void {
			++_attempt;

			connection.logger.debug("Recovery: Attempt to connect ({})", _attempt);
			connection.openChannel();
		}
	}
}
