package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.CsiException;
	import org.shypl.asak.timeline.GlobalTimeline;
	import org.shypl.asak.timeline.TimelineTask;

	internal class ConnectionReducer implements ChannelOpenHandler {
		private var _connection:Connection;
		private var _attempt:int;
		private var _timeoutTask:TimelineTask;
		private var _attemptTask:TimelineTask;

		public function ConnectionReducer(connection:Connection, reconnectTimeout:int) {
			_connection = connection;
			_timeoutTask = GlobalTimeline.schedule(1000 * 60 * reconnectTimeout, handleTimeout);
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
				throw new CsiException("Api connection broken, reconnect attempts is limited", error);
			}
			else {
				nextAttempt();
			}
		}

		private function handleTimeout():void {
			_connection.doClose(ConnectionCloseReason.RECONNECT_TIMEOUT_EXPIRED);
			throw new CsiException("Api connection broken, reconnect timeout expired");
		}

		private function nextAttempt():void {
			_attemptTask = (++_attempt == 1) ? GlobalTimeline.forNextFrame(run) : GlobalTimeline.schedule(1000, run);
		}

		private function run():void {
			_connection.getLogger().info("Reconnect");
			_connection.openChannel(this);
		}
	}
}
