package org.shypl.biser.api.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.api.ApiException;
	import org.shypl.common.timer.Timer;
	import org.shypl.common.timer.TimerTask;

	internal class ConnectionReducer implements ChannelOpenHandler {
		private var _connection:Connection;
		private var _attempt:int;
		private var _timeoutTask:TimerTask;
		private var _attemptTask:TimerTask;

		public function ConnectionReducer(connection:Connection) {
			_connection = connection;

			_timeoutTask = Timer.schedule(1000 * 60 * 10, handleTimeout);
			nextAttempt();
		}

		public function stop():void {
			if (_attemptTask) {
				_attemptTask.cancel();
				_attemptTask = null;
			}
			if (_timeoutTask) {
				_timeoutTask.cancel();
				_timeoutTask = null;
			}
			_connection = null;
		}

		public function handleOpen(channel:Channel):ChannelHandler {
			return _connection.reconnect(channel);
		}

		public function handleError(error:Error):void {
			if (_attempt == 300) {
				_connection.doClose(ConnectionCloseReason.RECONNECT_ERROR);
				throw new ApiException("Api connection broken, reconnect attempts is limited", error);
			}
			else {
				nextAttempt();
			}
		}

		private function handleTimeout():void {
			_connection.doClose(ConnectionCloseReason.RECONNECT_TIMEOUT_EXPIRED);
			throw new ApiException("Api connection broken, reconnect timeout expired");
		}

		private function nextAttempt():void {
			_attemptTask = ++_attempt == 1 ? Timer.forNextFrame(run) : Timer.schedule(1000, run);
		}

		private function run():void {
			_connection.getLogger().info("Reconnect");
			_connection.openChannel(this);
		}
	}
}
