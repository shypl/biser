package org.shypl.biser.csi;

public enum ConnectionCloseReason {
	NONE,
	SERVER_SHUTDOWN,
	ACTIVITY_TIMEOUT_EXPIRED,
	AUTHORIZATION_REJECT,
	RECOVERY_REJECT,
	CONCURRENT,
	PROTOCOL_BROKEN,
	SERVER_ERROR;

	public static byte getProtocolFlag(ConnectionCloseReason reason) {
		switch (reason) {
			case NONE:
				return Protocol.CLOSE;
			case SERVER_SHUTDOWN:
				return Protocol.CLOSE_SERVER_SHUTDOWN;
			case ACTIVITY_TIMEOUT_EXPIRED:
				return Protocol.CLOSE_ACTIVITY_TIMEOUT_EXPIRED;
			case AUTHORIZATION_REJECT:
				return Protocol.CLOSE_AUTHORIZATION_REJECT;
			case RECOVERY_REJECT:
				return Protocol.CLOSE_RECOVERY_REJECT;
			case CONCURRENT:
				return Protocol.CLOSE_CONCURRENT;
			case PROTOCOL_BROKEN:
				return Protocol.CLOSE_PROTOCOL_BROKEN;
			case SERVER_ERROR:
				return Protocol.CLOSE_SERVER_ERROR;
			default:
				throw new IllegalArgumentException();
		}
	}
}
