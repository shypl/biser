package org.shypl.biser.io

import org.shypl.biser.io.Bytes.x00
import org.shypl.biser.io.Bytes.x01

class DataWriter(
	private val stream: OutputData
) {
	private val buffer = ByteArray(9)
	
	fun writeBoolean(value: Boolean) {
		stream.writeByte(if (value) x01 else x00)
	}
	
	fun writeByte(value: Byte) {
		stream.writeByte(value)
	}
	
	fun writeInt(value: Int) {
		when (value) {
			-1        -> stream.writeByte(0xFF.toByte())
			in 0..253 -> stream.writeByte(value.toByte())
			else      -> {
				buffer[0] = 0xFE.toByte()
				buffer[1] = value.ushr(24).toByte()
				buffer[2] = value.ushr(16).toByte()
				buffer[3] = value.ushr(8).toByte()
				buffer[4] = value.toByte()
				stream.writeBytes(buffer, 5)
			}
		}
	}
	
	fun writeLong(value: Long) {
		when (value) {
			-1L       -> stream.writeByte(0xFF.toByte())
			in 0..253 -> stream.writeByte(value.toByte())
			else      -> {
				buffer[0] = 0xFE.toByte()
				buffer[1] = value.ushr(56).toByte()
				buffer[2] = value.ushr(48).toByte()
				buffer[3] = value.ushr(40).toByte()
				buffer[4] = value.ushr(32).toByte()
				buffer[5] = value.ushr(24).toByte()
				buffer[6] = value.ushr(16).toByte()
				buffer[7] = value.ushr(8).toByte()
				buffer[8] = value.toByte()
				stream.writeBytes(buffer, 9)
			}
		}
	}
	
	fun writeDouble(value: Double) {
		val l = value.toRawBits()
		buffer[0] = l.ushr(56).toByte()
		buffer[1] = l.ushr(48).toByte()
		buffer[2] = l.ushr(40).toByte()
		buffer[3] = l.ushr(32).toByte()
		buffer[4] = l.ushr(24).toByte()
		buffer[5] = l.ushr(16).toByte()
		buffer[6] = l.ushr(8).toByte()
		buffer[7] = l.toByte()
		stream.writeBytes(buffer, 8)
	}
	
	fun writeBytes(value: ByteArray) {
		writeInt(value.size)
		stream.writeBytes(value)
	}
	
	fun writeString(value: String) {
		writeBytes(PlatformUtils.encodeStringUtf8(value))
	}
	
	fun writeEnum(value: Enum<*>) {
		writeInt(value.ordinal)
	}
	
	fun <E> writeList(value: List<E>, encoder: Encoder<E>) {
		writeInt(value.size)
		value.forEach { encoder.invoke(this, it) }
	}
	
	fun writeEntity(value: Entity) {
		writeInt(value._id())
		value._encode(this)
	}
	
	fun <T> write(value: T, encoder: Encoder<T>) {
		encoder.invoke(this, value)
	}
}