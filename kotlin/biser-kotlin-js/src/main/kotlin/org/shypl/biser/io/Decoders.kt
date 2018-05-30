package org.shypl.biser.io

import kotlin.reflect.KClass

object Decoders {
	val BYTE: Decoder<Byte> = DataReader::readByte
	val BOOLEAN: Decoder<Boolean> = DataReader::readBoolean
	val INT: Decoder<Int> = DataReader::readInt
	val LONG: Decoder<Long> = DataReader::readLong
	val DOUBLE: Decoder<Double> = DataReader::readDouble
	val BYTES: Decoder<ByteArray> = DataReader::readBytes
	val STRING: Decoder<String> = DataReader::readString
	
	fun <T : Enum<T>> enum(type: KClass<T>): Decoder<T> {
		return { it.readEnum(type) }
	}
	
	fun <T : Any> list(decoder: Decoder<T>): Decoder<List<T>> {
		return { it.readList(decoder) }
	}
	
	inline fun <reified T : Enum<T>> enum(): Decoder<T> {
		return enum(T::class)
	}
	
	fun <T: Entity> entity(factory: (id: Int) -> T): Decoder<T> {
		return {
			factory(it.readInt()).apply {
				_decode(it)
			}
		}
	}
}