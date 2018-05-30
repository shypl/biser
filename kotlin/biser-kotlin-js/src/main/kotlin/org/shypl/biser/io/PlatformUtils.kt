package org.shypl.biser.io

import kotlin.reflect.KClass

object PlatformUtils {
	fun encodeStringUtf8(value: String): ByteArray {
		// https://gist.github.com/pascaldekloe/62546103a1576803dade9269ccf76330
		
		val l = value.length
		val bytes = ByteArray(l * 4)
		var i = 0
		var p = 0
		
		while (p != l) {
			var c: Int = value.getChar(p)
			if (c < 128) {
				bytes[i++] = c.toByte()
			} else {
				if (c < 2048) {
					bytes[i++] = (c shr 6 or 192).toByte()
				} else {
					@Suppress("ConvertTwoComparisonsToRangeCheck")
					if (c > 0xd7ff && c < 0xdc00) {
						if (++p == l) {
							throw IllegalArgumentException("UTF-8 encode: incomplete surrogate pair")
						}
						val c2: Int = value.getChar(p)
						
						if (c2 < 0xdc00 || c2 > 0xdfff) {
							throw IllegalArgumentException("UTF-8 encode: second char code 0x${c2.asDynamic().toString(16)} at index $p in surrogate pair out of range")
						}
						
						c = 0x10000 + (c and 0x03ff shl 10) + (c2 and 0x03ff)
						bytes[i++] = (c shr 18 or 240).toByte()
						bytes[i++] = (c shr 12 and 63 or 128).toByte()
					} else {
						bytes[i++] = (c shr 12 or 224).toByte()
					}
					bytes[i++] = (c shr 6 and 63 or 128).toByte()
				}
				bytes[i++] = (c and 63 or 128).toByte()
			}
			++p
		}
		
		return bytes.copyOf(i)
	}
	
	fun decodeStringUtf8(value: ByteArray): String {
		// https://gist.github.com/pascaldekloe/62546103a1576803dade9269ccf76330
		
		val l = value.size
		var s = ""
		var i = 0
		while (i < l) {
			var c = value[i++].toUint()
			if (c > 127) {
				when (c) {
					in 192..223 -> {
						if (i >= l) {
							throw IllegalArgumentException("UTF-8 decode: incomplete 2-byte sequence")
						}
						c = ((c and 31) shl 6) or
							(value[i].toUint() and 63)
					}
					in 224..239 -> {
						if (i + 1 >= l) {
							throw IllegalArgumentException("UTF-8 decode: incomplete 3-byte sequence")
						}
						c = ((c and 15) shl 12) or
							((value[i].toUint() and 63) shl 6) or
							(value[++i].toUint() and 63)
					}
					in 240..247 -> {
						if (i + 2 >= l) {
							throw IllegalArgumentException("UTF-8 decode: incomplete 4-byte sequence")
						}
						c = ((c and 7) shl 18) or
							((value[i].toUint() and 63) shl 12) or
							((value[++i].toUint() and 63) shl 6) or
							(value[++i].toUint() and 63)
					}
					else        -> throw IllegalArgumentException("UTF-8 decode: unknown multibyte start 0x${c.asDynamic().toString(16)} at index ${i - 1}")
				}
				++i
			}
			
			when {
				c <= 0xffff   -> s += c.asChar()
				c <= 0x10ffff -> {
					c -= 0x10000
					s += (c shr 10 or 0xd800).asChar() + (c and 0x3FF or 0xdc00).asChar()
				}
				else          -> throw IllegalArgumentException("UTF-8 decode: code point 0x${c.asDynamic().toString(16)} exceeds UTF-16 reach")
			}
		}
		return s
	}
	
	private fun String.getChar(i: Int): Int {
		return this.asDynamic().charCodeAt(i).unsafeCast<Int>()
	}
	
	private fun Int.asChar(): String {
		return js("String").fromCharCode(this).unsafeCast<String>()
	}
	
	fun <E : Enum<E>> getEnumValue(type: KClass<E>, ordinal: Int): E {
		return type.js.asDynamic().values()[ordinal].unsafeCast<E>()
	}
	
	fun copyByteArray(src: ByteArray, srcPos: Int, dest: ByteArray, destPos: Int, length: Int) {
		for ((o, i) in (srcPos until (srcPos + length)).withIndex()) {
			dest[destPos + o] = src[i]
		}
	}
}