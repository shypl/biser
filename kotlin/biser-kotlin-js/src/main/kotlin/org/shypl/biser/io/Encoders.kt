package org.shypl.biser.io

object Encoders {
	val BOOLEAN: Encoder<Boolean> = DataWriter::writeBoolean
	val BYTE: Encoder<Byte> = DataWriter::writeByte
	val INT: Encoder<Int> = DataWriter::writeInt
	val LONG: Encoder<Long> = DataWriter::writeLong
	val DOUBLE: Encoder<Double> = DataWriter::writeDouble
	val BYTES: Encoder<ByteArray> = DataWriter::writeBytes
	val STRING: Encoder<String> = DataWriter::writeString
	val ENUM: Encoder<Enum<*>> = DataWriter::writeEnum
	val ENTITY: Encoder<Entity> = DataWriter::writeEntity
	
	fun <E : Any> list(encoder: Encoder<E>): Encoder<List<E>> {
		return { writer, value -> writer.writeList(value, encoder) }
	}
}