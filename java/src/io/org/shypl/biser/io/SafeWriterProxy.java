package org.shypl.biser.io;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class SafeWriterProxy implements BiserWriter {
	protected final BiserWriter writer;

	public SafeWriterProxy(BiserWriter writer) {
		this.writer = writer;
	}

	@Override
	public void writeByte(byte value) {
		try {
			writer.writeByte(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeBool(boolean value) {
		try {
			writer.writeBool(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeInt(int value) {
		try {
			writer.writeInt(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeUint(long value) {
		try {
			writer.writeUint(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeLong(long value) {
		try {
			writer.writeLong(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeUlong(long value) {
		try {
			writer.writeUlong(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeDouble(double value) {
		try {
			writer.writeDouble(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeBytes(byte[] value) {
		try {
			writer.writeBytes(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeString(String value) {
		try {
			writer.writeString(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeDate(Date value) {
		try {
			writer.writeDate(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeEnum(Enum<?> value) {
		try {
			writer.writeEnum(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeEntity(Entity value) {
		try {
			writer.writeEntity(value);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeByteArray(byte[] array) {
		try {
			writer.writeByteArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeBoolArray(boolean[] array) {
		try {
			writer.writeBoolArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeIntArray(int[] array) {
		try {
			writer.writeIntArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeUintArray(int[] array) {
		try {
			writer.writeUintArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeLongArray(long[] array) {
		try {
			writer.writeLongArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeUlongArray(long[] array) {
		try {
			writer.writeUlongArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeDoubleArray(double[] array) {
		try {
			writer.writeDoubleArray(array);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E> void writeArray(E[] array, Encoder<E> elementEncoder) {
		try {
			writer.writeArray(array, elementEncoder);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <K, V> void writeMap(Map<K, V> map, Encoder<K> keyEncoder, Encoder<V> valueEncoder) {
		try {
			writer.writeMap(map, keyEncoder, valueEncoder);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
