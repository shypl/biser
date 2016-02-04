package org.shypl.biser.io;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class ByteArrayWriter implements BiserWriter {
	private final ByteArrayOutputStream stream;
	private final SafeWriterProxy       writer;

	public ByteArrayWriter() {
		stream = new ByteArrayOutputStream();
		writer = new SafeWriterProxy(new StreamWriter(stream));
	}

	public byte[] toByteArray() {
		return stream.toByteArray();
	}

	public int size() {
		return stream.size();
	}

	public void clear() {
		stream.clear();
	}

	public boolean isEmpty() {
		return stream.isEmpty();
	}

	public boolean isNotEmpty() {
		return stream.isNotEmpty();
	}

	@Override
	public void writeByte(byte value) {
		writer.writeByte(value);
	}

	@Override
	public void writeBool(boolean value) {
		writer.writeBool(value);
	}

	@Override
	public void writeInt(int value) {
		writer.writeInt(value);
	}

	@Override
	public void writeUint(long value) {
		writer.writeUint(value);
	}

	@Override
	public void writeLong(long value) {
		writer.writeLong(value);
	}

	@Override
	public void writeUlong(long value) {
		writer.writeUlong(value);
	}

	@Override
	public void writeDouble(double value) {
		writer.writeDouble(value);
	}

	@Override
	public void writeBytes(byte[] value) {
		writer.writeBytes(value);
	}

	@Override
	public void writeString(String value) {
		writer.writeString(value);
	}

	@Override
	public void writeDate(Date value) {
		writer.writeDate(value);
	}

	@Override
	public void writeEnum(Enum<?> value) {
		writer.writeEnum(value);
	}

	@Override
	public void writeEntity(Entity value) {
		writer.writeEntity(value);
	}

	@Override
	public void writeByteArray(byte[] array) {
		writer.writeByteArray(array);
	}

	@Override
	public void writeBoolArray(boolean[] array) {
		writer.writeBoolArray(array);
	}

	@Override
	public void writeIntArray(int[] array) {
		writer.writeIntArray(array);
	}

	@Override
	public void writeUintArray(int[] array) {
		writer.writeUintArray(array);
	}

	@Override
	public void writeLongArray(long[] array) {
		writer.writeLongArray(array);
	}

	@Override
	public void writeUlongArray(long[] array) {
		writer.writeUlongArray(array);
	}

	@Override
	public void writeDoubleArray(double[] array) {
		writer.writeDoubleArray(array);
	}

	@Override
	public <E> void writeArray(E[] array, Encoder<E> elementEncoder) {
		writer.writeArray(array, elementEncoder);
	}

	@Override
	public <K, V> void writeMap(Map<K, V> map, Encoder<? super K> keyEncoder, Encoder<? super V> valueEncoder) {
		writer.writeMap(map, keyEncoder, valueEncoder);
	}

	@Override
	public <E> void writeCollection(Collection<E> collection, Encoder<? super E> elementEncoder) throws IOException {
		writer.writeCollection(collection, elementEncoder);
	}
}
