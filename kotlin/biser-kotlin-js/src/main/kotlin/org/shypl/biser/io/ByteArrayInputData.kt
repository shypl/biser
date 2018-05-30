package org.shypl.biser.io


class ByteArrayInputData(
	private var array: ByteArray
) : InputData {
	private var cursor: Int = 0
	
	override val readableBytes: Int
		get() = array.size - cursor
	
	override fun isReadable(): Boolean {
		return cursor < array.size
	}
	
	override fun readByte(): Byte {
		return array[cursor++]
	}
	
	override fun readInt(): Int {
		val v = array[cursor + 0].toUint().shl(24) +
			array[cursor + 1].toUint().shl(16) +
			array[cursor + 2].toUint().shl(8) +
			array[cursor + 3].toUint()
		
		cursor += 4
		
		return v
	}
	
	override fun readBytes(target: ByteArray, len: Int) {
		PlatformUtils.copyByteArray(array, cursor, target, 0, len)
		cursor += len
	}
	
	override fun readBytes(target: OutputData, len: Int) {
		target.writeBytes(array, cursor, len)
		cursor += len
	}
	
	override fun toByteArray(): ByteArray {
		return array.copyOf()
	}
}
