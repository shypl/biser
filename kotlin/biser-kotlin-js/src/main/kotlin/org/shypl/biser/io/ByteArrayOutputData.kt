package org.shypl.biser.io

class ByteArrayOutputData(capacity: Int = 32) : OutputData {
	private var array = ByteArray(capacity)
	private var cursor: Int = 0
	
	override fun isEmpty(): Boolean {
		return cursor == 0
	}
	
	override fun writeByte(value: Byte) {
		increaseCapacity(1)
		array[cursor++] = value
	}
	
	override fun writeInt(value: Int) {
		increaseCapacity(4)
		array[cursor + 0] = value.ushr(24).toByte()
		array[cursor + 1] = value.ushr(16).toByte()
		array[cursor + 2] = value.ushr(8).toByte()
		array[cursor + 3] = value.toByte()
		cursor += 4
	}
	
	override fun writeBytes(value: ByteArray, len: Int) {
		writeBytes(value, 0, len)
	}
	
	override fun writeBytes(value: ByteArray, pos: Int, len: Int) {
		increaseCapacity(len)
		PlatformUtils.copyByteArray(value, pos, array, cursor, len)
		cursor += len
	}
	
	override fun clear() {
		cursor = 0
	}
	
	override fun toByteArray(): ByteArray {
		return array.copyOf(cursor)
	}
	
	private fun increaseCapacity(v: Int) {
		val newCapacity = cursor + v
		var capacity = array.size
		
		if (newCapacity < 0) {
			throw RuntimeException()
		}
		
		if (capacity < newCapacity) {
			capacity += capacity shr 1
			if (capacity < newCapacity) {
				capacity = newCapacity
			} else if (capacity < 0) {
				capacity = Int.MAX_VALUE
			}
			array = array.copyOf(capacity)
		}
	}
}