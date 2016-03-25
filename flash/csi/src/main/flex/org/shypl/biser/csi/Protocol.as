package org.shypl.biser.csi {
	public final class Protocol {
		public static const AUTHORIZATION:int = 0x01;
		public static const RECOVERY:int = 0x02;
		public static const PING:int = 0x03;
		public static const MESSAGE_ODD:int = 0x04;
		public static const MESSAGE_EVEN:int = 0x05;
		public static const MESSAGE_ODD_RECEIVED:int = 0x06;
		public static const MESSAGE_EVEN_RECEIVED:int = 0x07;
		public static const SERVER_DOWN_TIMEOUT:int = 0x08;

		public static const CLOSE:int = 0x10;
		public static const CLOSE_SERVER_SHUTDOWN:int = 0x11;
		public static const CLOSE_ACTIVITY_TIMEOUT_EXPIRED:int = 0x12;
		public static const CLOSE_RECOVERY_REJECT:int = 0x13;
		public static const CLOSE_AUTHORIZATION_REJECT:int = 0x14;
		public static const CLOSE_CONCURRENT:int = 0x15;
		public static const CLOSE_PROTOCOL_BROKEN:int = 0x16;
		public static const CLOSE_SERVER_ERROR:int = 0x17;
	}
}
