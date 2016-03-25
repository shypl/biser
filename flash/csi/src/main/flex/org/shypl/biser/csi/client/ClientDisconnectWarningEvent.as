package org.shypl.biser.csi.client {
	public class ClientDisconnectWarningEvent extends ClientEvent {
		public static const CLIENT_DISCONNECT_WARNING:String = "clientDisconnectWarning";

		private var _timeout:int;

		public function ClientDisconnectWarningEvent(timeout:int) {
			super(CLIENT_DISCONNECT_WARNING);
			_timeout = timeout;
		}

		public function get timeout():int {
			return _timeout;
		}
	}
}
