package org.shypl.biser.csi;

public final class Protocol {
	public static final byte CROSS_DOMAIN_POLICY     = 0x3c; // <
	public static final byte AUTHORIZATION           = 0x01;
	public static final byte MESSAGE_ODD             = 0x02;
	public static final byte MESSAGE_EVEN            = 0x03;
	public static final byte MESSAGE_ODD_RECEIVED    = 0x04;
	public static final byte MESSAGE_EVEN_RECEIVED   = 0x05;
	public static final byte PING                    = 0x06;
	public static final byte RECOVERY                = 0x07;
	public static final byte SERVER_SHUTDOWN_TIMEOUT = 0x08;

	public static final byte CLOSE                          = 0x10;
	public static final byte CLOSE_SERVER_SHUTDOWN          = 0x11;
	public static final byte CLOSE_ACTIVITY_TIMEOUT_EXPIRED = 0x12;
	public static final byte CLOSE_AUTHORIZATION_REJECT     = 0x13;
	public static final byte CLOSE_RECOVERY_REJECT          = 0x14;
	public static final byte CLOSE_CONCURRENT               = 0x15;
	public static final byte CLOSE_PROTOCOL_BROKEN          = 0x16;
	public static final byte CLOSE_SERVER_ERROR             = 0x17;

	private Protocol() {
	}
}
