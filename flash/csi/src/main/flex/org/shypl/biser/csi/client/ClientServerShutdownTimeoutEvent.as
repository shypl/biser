package org.shypl.biser.csi.client {
	public class ClientServerShutdownTimeoutEvent extends ClientConnectionEvent {
		public static const CLIENT_SERVER_SHUTDOWN_TIMEOUT:String = "clientServerShutdownTimeout";

		private var _timeout:int;

		public function ClientServerShutdownTimeoutEvent(timeout:int) {
			super(CLIENT_SERVER_SHUTDOWN_TIMEOUT);
			_timeout = timeout;
		}

		public function get timeoutSeconds():int {
			return _timeout;
		}
	}
}
