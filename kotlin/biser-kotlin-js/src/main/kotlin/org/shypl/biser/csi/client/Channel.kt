package org.shypl.biser.csi.client

interface Channel {
	fun writeByte(byte: Byte)
	
	fun writeBytes(bytes: ByteArray)
	
	fun close()
}