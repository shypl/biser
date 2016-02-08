package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.Protocol;
	import org.shypl.asak.lang.Enum;

	public final class ConnectionCloseReason extends Enum {
		public static const CLOSE:ConnectionCloseReason = new ConnectionCloseReason("CLOSE");
		public static const SERVER_ERROR:ConnectionCloseReason = new ConnectionCloseReason("SERVER_ERROR");
		public static const PROTOCOL_BROKEN:ConnectionCloseReason = new ConnectionCloseReason("PROTOCOL_BROKEN");
		public static const CONNECT_REJECT:ConnectionCloseReason = new ConnectionCloseReason("CONNECT_REJECT");
		public static const SERVER_DOWN:ConnectionCloseReason = new ConnectionCloseReason("SERVER_DOWN");
		public static const AUTHORIZATION_TIMEOUT_EXPIRED:ConnectionCloseReason = new ConnectionCloseReason("AUTHORIZATION_TIMEOUT_EXPIRED");
		public static const CONCURRENT_CONNECT:ConnectionCloseReason = new ConnectionCloseReason("CONCURRENT_CONNECT");
		public static const RECONNECT_TIMEOUT_EXPIRED:ConnectionCloseReason = new ConnectionCloseReason("RECONNECT_TIMEOUT_EXPIRED");
		public static const RECONNECT_REJECT:ConnectionCloseReason = new ConnectionCloseReason("RECONNECT_REJECT");

		public static const UNDEFINED:ConnectionCloseReason = new ConnectionCloseReason("UNDEFINED");
		public static const CLIENT_ERROR:ConnectionCloseReason = new ConnectionCloseReason("CLIENT_ERROR");
		public static const RECONNECT_ERROR:ConnectionCloseReason = new ConnectionCloseReason("RECONNECT_ERROR");

		public static function defineByFlag(flag:int):ConnectionCloseReason {
			switch (flag) {
				case Protocol.CLOSE:
					return CLOSE;
				case Protocol.CLOSE_SERVER_ERROR:
					return SERVER_ERROR;
				case Protocol.CLOSE_PROTOCOL_BROKEN:
					return PROTOCOL_BROKEN;
				case Protocol.CLOSE_CONNECT_REJECT:
					return CONNECT_REJECT;
				case Protocol.CLOSE_SERVER_DOWN:
					return SERVER_DOWN;
				case Protocol.CLOSE_AUTHORIZATION_TIMEOUT_EXPIRED:
					return AUTHORIZATION_TIMEOUT_EXPIRED;
				case Protocol.CLOSE_CONCURRENT_CONNECT:
					return CONCURRENT_CONNECT;
				case Protocol.CLOSE_RECONNECT_TIMEOUT_EXPIRED:
					return RECONNECT_TIMEOUT_EXPIRED;
				case Protocol.CLOSE_RECONNECT_REJECT:
					return RECONNECT_REJECT;
				default:
					return UNDEFINED;
			}
		}

		public function ConnectionCloseReason(name:String) {
			super(name);
		}
	}
}
