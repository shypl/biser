package org.shypl.biser.api;

public final class Protocol {
	public static final byte CROSS_DOMAIN_POLICY   = 0x3c; // <
	public static final byte CONNECT               = 0x01;
	public static final byte CONNECT_SUCCESS       = 0x02;
	public static final byte RECONNECT             = 0x03;
	public static final byte CLOSE                 = 0x04;
	public static final byte PING                  = 0x05;
	public static final byte MESSAGE_ODD           = 0x06;
	public static final byte MESSAGE_EVEN          = 0x07;
	public static final byte MESSAGE_ODD_RECEIVED  = 0x08;
	public static final byte MESSAGE_EVEN_RECEIVED = 0x09;

	public static final byte CLOSE_SERVER_ERROR                  = 0x50;
	public static final byte CLOSE_PROTOCOL_BROKEN               = 0x51;
	public static final byte CLOSE_CONNECT_REJECT                = 0x52;
	public static final byte CLOSE_SERVER_DOWN                   = 0x53;
	public static final byte CLOSE_AUTHORIZATION_TIMEOUT_EXPIRED = 0x54;
	public static final byte CLOSE_CONCURRENT_CONNECT            = 0x55;
	public static final byte CLOSE_RECONNECT_TIMEOUT_EXPIRED     = 0x56;
	public static final byte CLOSE_RECONNECT_REJECT              = 0x57;

	private Protocol() {}
}
