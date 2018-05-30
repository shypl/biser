package org.shypl.biser.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get

class ArrayBufferInputData(buffer: ArrayBuffer) : InputData {
	private val array = Int8Array(buffer)
	
	private var cursor: Int = 0
	override val readableBytes: Int
		get() = array.length - cursor
	
	override fun isReadable(): Boolean {
		return cursor < array.length
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
		repeat(len) { target[it] = readByte() }
	}
	
	override fun readBytes(target: OutputData, len: Int) {
		repeat(len) { target.writeByte(readByte()) }
	}
	
	override fun toByteArray(): ByteArray {
		val a = ByteArray(array.length)
		repeat(a.size) { a[it] = array[it] }
		return a
	}
}