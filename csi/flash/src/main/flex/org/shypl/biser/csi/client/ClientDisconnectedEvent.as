package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.ConnectionCloseReason;

	public class ClientDisconnectedEvent extends ClientEvent {
		public static const CLIENT_DISCONNECTED:String = "clientDisconnected";

		private var _reason:ConnectionCloseReason;

		public function ClientDisconnectedEvent(reason:ConnectionCloseReason) {
			super(CLIENT_DISCONNECTED);
			_reason = reason;
		}

		public function get reason():ConnectionCloseReason {
			return _reason;
		}
	}
}
