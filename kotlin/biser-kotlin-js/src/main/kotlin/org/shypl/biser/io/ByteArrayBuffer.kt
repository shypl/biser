package org.shypl.biser.io

class ByteArrayBuffer(capacity: Int = 32) : InputData, OutputData {
	private var array = ByteArray(capacity)
	
	private var writeCursor: Int = 0
	private var readCursor: Int = 0
	
	override val readableBytes: Int
		get() = writeCursor - readCursor
	
	override fun isEmpty(): Boolean {
		return writeCursor == 0
	}
	
	override fun isReadable(): Boolean {
		return readCursor < array.size
	}
	
	override fun readByte(): Byte {
		return array[readCursor++]
	}
	
	override fun readInt(): Int {
		val v = array[readCursor + 0].toUint().shl(24) +
			array[readCursor + 1].toUint().shl(16) +
			array[readCursor + 2].toUint().shl(8) +
			array[readCursor + 3].toUint()
		
		readCursor += 4
		
		return v
	}
	
	override fun readBytes(target: ByteArray, len: Int) {
		PlatformUtils.copyByteArray(array, readCursor, target, 0, len)
		readCursor += len
	}
	
	override fun readBytes(target: OutputData, len: Int) {
		target.writeBytes(array, readCursor, len)
		readCursor += len
	}
	
	override fun writeByte(value: Byte) {
		increaseCapacity(1)
		array[writeCursor++] = value
	}
	
	override fun writeInt(value: Int) {
		increaseCapacity(4)
		array[writeCursor + 0] = value.ushr(24).toByte()
		array[writeCursor + 1] = value.ushr(16).toByte()
		array[writeCursor + 2] = value.ushr(8).toByte()
		array[writeCursor + 3] = value.toByte()
		writeCursor += 4
	}
	
	override fun writeBytes(value: ByteArray, len: Int) {
		writeBytes(value, 0, len)
	}
	
	override fun writeBytes(value: ByteArray, pos: Int, len: Int) {
		increaseCapacity(len)
		PlatformUtils.copyByteArray(value, pos, array, writeCursor, len)
		writeCursor += len
	}
	
	override fun clear() {
		writeCursor = 0
		readCursor = 0
	}
	
	override fun toByteArray(): ByteArray {
		return array.copyOf(writeCursor)
	}
	
	private fun increaseCapacity(v: Int) {
		val newCapacity = writeCursor + v
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
