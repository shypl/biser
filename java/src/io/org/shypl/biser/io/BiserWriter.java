package org.shypl.biser.io;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public interface BiserWriter {
	void writeByte(byte value) throws IOException;

	void writeBool(boolean value) throws IOException;

	void writeInt(int value) throws IOException;

	void writeUint(long value) throws IOException;

	void writeLong(long value) throws IOException;

	void writeUlong(long value) throws IOException;

	void writeDouble(double value) throws IOException;

	void writeBytes(byte[] value) throws IOException;

	void writeString(String value) throws IOException;

	void writeDate(Date value) throws IOException;

	void writeEnum(Enum<?> value) throws IOException;

	void writeEntity(Entity value) throws IOException;

	void writeByteArray(byte[] array) throws IOException;

	void writeBoolArray(boolean[] array) throws IOException;

	void writeIntArray(int[] array) throws IOException;

	void writeUintArray(int[] array) throws IOException;

	void writeLongArray(long[] array) throws IOException;

	void writeUlongArray(long[] array) throws IOException;

	void writeDoubleArray(double[] array) throws IOException;

	<E> void writeArray(E[] array, Encoder<E> elementEncoder) throws IOException;

	<K, V> void writeMap(Map<K, V> map, Encoder<K> keyEncoder, Encoder<V> valueEncoder) throws IOException;
}
