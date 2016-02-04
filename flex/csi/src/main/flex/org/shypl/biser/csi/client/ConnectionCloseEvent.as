package org.shypl.biser.csi.client {
	import flash.events.Event;

	public class ConnectionCloseEvent extends Event {
		public static const CONNECTION_CLOSE:String = "CONNECTION_CLOSE";

		private var _reason:ConnectionCloseReason;

		public function ConnectionCloseEvent(reason:ConnectionCloseReason) {
			super(CONNECTION_CLOSE);
			_reason = reason;
		}

		public function get reason():ConnectionCloseReason {
			return _reason;
		}
	}
}
