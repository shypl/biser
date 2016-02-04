package org.shypl.biser.io;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public interface Encoder<T> {
	Encoder<Byte>    BYTE   = (value, writer) -> writer.writeByte(value);
	Encoder<Boolean> BOOL   = (value, writer) -> writer.writeBool(value);
	Encoder<Integer> INT    = (value, writer) -> writer.writeInt(value);
	Encoder<Long>    UINT   = (value, writer) -> writer.writeUint(value);
	Encoder<Long>    LONG   = (value, writer) -> writer.writeLong(value);
	Encoder<Long>    ULONG  = (value, writer) -> writer.writeUlong(value);
	Encoder<Double>  DOUBLE = (value, writer) -> writer.writeDouble(value);
	Encoder<byte[]>  BYTES  = (value, writer) -> writer.writeBytes(value);
	Encoder<String>  STRING = (value, writer) -> writer.writeString(value);
	Encoder<Date>    DATE   = (value, writer) -> writer.writeDate(value);
	Encoder<Enum<?>> ENUM   = (value, writer) -> writer.writeEnum(value);
	Encoder<Entity>  ENTITY = (value, writer) -> writer.writeEntity(value);

	Encoder<byte[]>    BYTE_ARRAY   = (value, writer) -> writer.writeByteArray(value);
	Encoder<boolean[]> BOOL_ARRAY   = (value, writer) -> writer.writeBoolArray(value);
	Encoder<int[]>     INT_ARRAY    = (value, writer) -> writer.writeIntArray(value);
	Encoder<int[]>     UINT_ARRAY   = (value, writer) -> writer.writeUintArray(value);
	Encoder<long[]>    LONG_ARRAY   = (value, writer) -> writer.writeLongArray(value);
	Encoder<long[]>    ULONG_ARRAY  = (value, writer) -> writer.writeUlongArray(value);
	Encoder<double[]>  DOUBLE_ARRAY = (value, writer) -> writer.writeDoubleArray(value);

	static <E> Encoder<E[]> forArray(Encoder<E> elementEncoder) {
		return new ArrayEncoder<>(elementEncoder);
	}

	static <K, V> Encoder<Map<K, V>> forMap(Encoder<K> keyEncoder, Encoder<V> valueEncoder) {
		return new MapEncoder<>(keyEncoder, valueEncoder);
	}

	void encode(T value, BiserWriter writer) throws IOException;
}
