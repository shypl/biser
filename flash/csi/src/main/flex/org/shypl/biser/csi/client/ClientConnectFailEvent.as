package org.shypl.biser.csi.client {
	import flash.events.Event;

	public class ClientConnectFailEvent extends Event {
		public static const CLIENT_CONNECT_FAIL:String = "clientConnectFail";

		private var _reason:Error;

		public function ClientConnectFailEvent(reason:Error) {
			super(CLIENT_CONNECT_FAIL);
			_reason = reason;
		}

		public function get reason():Error {
			return _reason;
		}
	}
}
