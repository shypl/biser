package org.shypl.biser.io

interface InputData {
	val readableBytes: Int
	
	fun isReadable(): Boolean
	
	fun readByte(): Byte
	
	fun readInt(): Int
	
	fun readBytes(target: ByteArray, len: Int = target.size)
	
	fun readBytes(target: OutputData, len: Int)
	
	fun toByteArray(): ByteArray
}