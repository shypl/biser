package org.shypl.biser.csi

enum class ConnectionCloseReason {
	NONE,
	SERVER_SHUTDOWN,
	ACTIVITY_TIMEOUT_EXPIRED,
	AUTHORIZATION_REJECT,
	RECOVERY_REJECT,
	RECOVERY_TIMEOUT_EXPIRED,
	CONCURRENT,
	PROTOCOL_BROKEN,
	SERVER_ERROR;
	
	companion object {
		fun getReason(protocolFlag: Byte): ConnectionCloseReason? {
			return when (protocolFlag) {
				Protocol.CLOSE                          -> NONE;
				Protocol.CLOSE_SERVER_SHUTDOWN          -> SERVER_SHUTDOWN;
				Protocol.CLOSE_ACTIVITY_TIMEOUT_EXPIRED -> ACTIVITY_TIMEOUT_EXPIRED;
				Protocol.CLOSE_AUTHORIZATION_REJECT     -> AUTHORIZATION_REJECT;
				Protocol.CLOSE_RECOVERY_REJECT          -> RECOVERY_REJECT;
				Protocol.CLOSE_CONCURRENT               -> CONCURRENT;
				Protocol.CLOSE_PROTOCOL_BROKEN          -> PROTOCOL_BROKEN;
				Protocol.CLOSE_SERVER_ERROR             -> SERVER_ERROR;
				else                                    -> null
			}
		}
	}
}