package org.shypl.biser.io

import org.shypl.biser.io.Bytes.x01
import kotlin.reflect.KClass

class DataReader(
	private val stream: InputData
) {
	private val buffer = ByteArray(8)
	
	fun readBoolean(): Boolean {
		return nextByte() == x01
	}
	
	fun readByte(): Byte {
		return nextByte()
	}
	
	fun readInt(): Int {
		val b = readByte().toUint()
		
		return when (b) {
			0xFF -> -1
			0xFE -> readRawInt()
			else -> b
		}
	}
	
	fun readLong(): Long {
		val b = readByte().toUint()
		
		when (b) {
			0xFF -> return -1
			0xFE -> return readRawLong()
			else -> return b.toLong()
		}
	}
	
	fun readDouble(): Double {
		return Double.fromBits(readRawLong())
	}
	
	fun readBytes(): ByteArray {
		val size = readInt()
		return when (size) {
			-1, 0 -> ByteArray(0)
			else  -> ByteArray(size).also { stream.readBytes(it) }
		}
	}
	
	fun readString(): String {
		return PlatformUtils.decodeStringUtf8(readBytes())
	}
	
	fun <E : Enum<E>> readEnum(type: KClass<E>): E {
		return PlatformUtils.getEnumValue(type, readInt())
	}
	
	fun <E> readList(decoder: Decoder<E>): List<E> {
		val size = readInt()
		return when (size) {
			-1, 0 -> emptyList()
			else  -> List(size) { decoder.invoke(this) }
		}
	}
	
	fun <E> read(decoder: Decoder<E>): E {
		return decoder.invoke(this)
	}
	
	private fun nextByte(): Byte {
		return stream.readByte()
	}
	
	private fun readRawInt(): Int {
		nextBytesToBuffer(4)
		
		return (buffer[0].toUint() shl 24) +
			(buffer[1].toUint() shl 16) +
			(buffer[2].toUint() shl 8) +
			(buffer[3].toUint())
	}
	
	
	private fun readRawLong(): Long {
		nextBytesToBuffer(8)
		
		return buffer[0].toLong().shl(56) +
			buffer[1].toUlong().shl(48) +
			buffer[2].toUlong().shl(40) +
			buffer[3].toUlong().shl(32) +
			buffer[4].toUlong().shl(24) +
			buffer[5].toUint().shl(16) +
			buffer[6].toUint().shl(8) +
			buffer[7].toUint()
	}
	
	private fun nextBytesToBuffer(len: Int) {
		stream.readBytes(buffer, len)
	}
	
	private fun Byte.toUlong(): Long {
		return toLong().and(0xFF)
	}
}