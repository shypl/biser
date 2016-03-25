package org.shypl.biser.csi {
	import org.shypl.common.lang.Enum;

	public class ConnectionCloseReason extends Enum {
		public static const NONE:ConnectionCloseReason = new ConnectionCloseReason("NONE");
		public static const SERVER_SHUTDOWN:ConnectionCloseReason = new ConnectionCloseReason("SERVER_SHUTDOWN");
		public static const ACTIVITY_TIMEOUT_EXPIRED:ConnectionCloseReason = new ConnectionCloseReason("ACTIVITY_TIMEOUT_EXPIRED");
		public static const AUTHORIZATION_REJECT:ConnectionCloseReason = new ConnectionCloseReason("AUTHORIZATION_REJECT");
		public static const RECOVERY_REJECT:ConnectionCloseReason = new ConnectionCloseReason("RECOVERY_REJECT");
		public static const RECOVERY_TIMEOUT_EXPIRED:ConnectionCloseReason = new ConnectionCloseReason("RECOVERY_TIMEOUT_EXPIRED");
		public static const CONCURRENT:ConnectionCloseReason = new ConnectionCloseReason("CONCURRENT");
		public static const PROTOCOL_BROKEN:ConnectionCloseReason = new ConnectionCloseReason("PROTOCOL_BROKEN");
		public static const SERVER_ERROR:ConnectionCloseReason = new ConnectionCloseReason("SERVER_ERROR");

		public static function getReason(protocolFlag:int):ConnectionCloseReason {
			switch (protocolFlag) {
				case Protocol.CLOSE:
					return NONE;
				case Protocol.CLOSE_SERVER_SHUTDOWN:
					return SERVER_SHUTDOWN;
				case Protocol.CLOSE_ACTIVITY_TIMEOUT_EXPIRED:
					return ACTIVITY_TIMEOUT_EXPIRED;
				case Protocol.CLOSE_AUTHORIZATION_REJECT:
					return AUTHORIZATION_REJECT;
				case Protocol.CLOSE_RECOVERY_REJECT:
					return RECOVERY_REJECT;
				case Protocol.CLOSE_CONCURRENT:
					return CONCURRENT;
				case Protocol.CLOSE_PROTOCOL_BROKEN:
					return PROTOCOL_BROKEN;
				case Protocol.CLOSE_SERVER_ERROR:
					return SERVER_ERROR;
				default:
					return null;
			}
		}

		public function ConnectionCloseReason(name:String) {
			super(name);
		}
	}
}
