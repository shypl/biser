package org.shypl.biser.api {
	public final class Protocol {
		public static const CONNECT:int = 0x01;
		public static const CONNECT_SUCCESS:int = 0x02;
		public static const RECONNECT:int = 0x03;
		public static const CLOSE:int = 0x04;
		public static const PING:int = 0x05;
		public static const MESSAGE_ODD:int = 0x06;
		public static const MESSAGE_EVEN:int = 0x07;
		public static const MESSAGE_ODD_RECEIVED:int = 0x08;
		public static const MESSAGE_EVEN_RECEIVED:int = 0x09;

		public static const CLOSE_SERVER_ERROR:int = 0x50;
		public static const CLOSE_PROTOCOL_BROKEN:int = 0x51;
		public static const CLOSE_CONNECT_REJECT:int = 0x52;
		public static const CLOSE_SERVER_DOWN:int = 0x53;
		public static const CLOSE_AUTHORIZATION_TIMEOUT_EXPIRED:int = 0x54;
		public static const CLOSE_CONCURRENT_CONNECT:int = 0x55;
		public static const CLOSE_RECONNECT_TIMEOUT_EXPIRED:int = 0x56;
		public static const CLOSE_RECONNECT_REJECT:int = 0x57;
	}
}
