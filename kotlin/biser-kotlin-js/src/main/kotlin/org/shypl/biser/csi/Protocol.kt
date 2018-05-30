package org.shypl.biser.csi

object Protocol {
	const val AUTHORIZATION = 0x01.toByte()
	const val MESSAGE = 0x02.toByte()
	const val MESSAGE_RECEIVED = 0x03.toByte()
	const val PING = 0x06.toByte()
	const val RECOVERY = 0x07.toByte()
	const val SERVER_SHUTDOWN_TIMEOUT = 0x08.toByte()
	
	const val CLOSE = 0x10.toByte()
	const val CLOSE_SERVER_SHUTDOWN = 0x11.toByte()
	const val CLOSE_ACTIVITY_TIMEOUT_EXPIRED = 0x12.toByte()
	const val CLOSE_AUTHORIZATION_REJECT = 0x13.toByte()
	const val CLOSE_RECOVERY_REJECT = 0x14.toByte()
	const val CLOSE_CONCURRENT = 0x15.toByte()
	const val CLOSE_PROTOCOL_BROKEN = 0x16.toByte()
	const val CLOSE_SERVER_ERROR = 0x17.toByte()
}