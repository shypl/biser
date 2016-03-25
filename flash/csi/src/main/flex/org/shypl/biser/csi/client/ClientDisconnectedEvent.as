package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.ConnectionCloseReason;

	public class ClientDisconnectedEvent extends ClientEvent {
		public static const DISCONNECTED:String = "disconnected";

		private var _reason:ConnectionCloseReason;

		public function ClientDisconnectedEvent(reason:ConnectionCloseReason) {
			super(DISCONNECTED);
			_reason = reason;
		}

		public function get reason():ConnectionCloseReason {
			return _reason;
		}
	}
}
