package org.shypl.biser.csi.client {
	public class ServerShutdownTimeoutEvent extends ClientEvent {
		public static const SERVER_SHUTDOWN_TIMEOUT:String = "serverShutdownTimeout";

		private var _timeout:int;

		public function ServerShutdownTimeoutEvent(timeout:int) {
			super(SERVER_SHUTDOWN_TIMEOUT);
			_timeout = timeout;
		}

		public function get timeoutSeconds():int {
			return _timeout;
		}
	}
}
