package org.shypl.biser.io

interface OutputData {
	fun writeByte(value: Byte)
	
	fun writeInt(value: Int)
	
	fun writeBytes(value: ByteArray, len: Int = value.size)
	
	fun writeBytes(value: ByteArray, pos: Int, len: Int)
	
	fun toByteArray(): ByteArray
	
	fun isEmpty(): Boolean
	
	fun clear()
}
