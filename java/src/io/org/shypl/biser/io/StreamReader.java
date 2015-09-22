package org.shypl.biser.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class StreamReader implements BiserReader {
	protected final InputStream stream;
	private byte[] buffer = new byte[8];

	public StreamReader(InputStream stream) {
		this.stream = stream;
	}

	@Override
	public byte readByte() throws IOException {
		return (byte)read();
	}

	@Override
	public boolean readBool() throws IOException {
		return read() == 0x01;
	}

	@Override
	public int readInt() throws IOException {
		int b0 = read();

		switch (b0) {
			case 0xFF:
				return -1;

			case 0xFE: {
				int b1 = stream.read();
				int b2 = stream.read();
				int b3 = stream.read();
				int b4 = stream.read();
				if ((b1 | b2 | b3 | b4) < 0) {
					throw new EOFException();
				}
				return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
			}

			default:
				return b0 & 0xFF;
		}
	}

	@Override
	public int readUint() throws IOException {
		return readInt();
	}

	@Override
	public long readLong() throws IOException {
		int b = read();

		switch (b) {
			case 0xFF:
				return -1;

			case 0xFE:
				return readRawLong();

			default:
				return b & 0xFF;
		}
	}

	@Override
	public long readUlong() throws IOException {
		return readLong();
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readRawLong());
	}

	@Override
	public byte[] readBytes() throws IOException {
		return readByteArray();
	}

	@Override
	public String readString() throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		if (size == 0) {
			return "";
		}

		if (buffer.length < size) {
			buffer = new byte[size];
		}

		readBytesToBuffer(size);

		return new String(buffer, 0, size, StandardCharsets.UTF_8);
	}

	@Override
	public Date readDate() throws IOException {
		long ms = readRawLong();

		if (ms == Long.MIN_VALUE) {
			return null;
		}

		return new Date(ms);
	}

	@Override
	public <E extends Enum<E>> E readEnum(Class<E> type) throws IOException {
		int ordinal = readInt();

		if (ordinal == -1) {
			return null;
		}

		E[] enums = type.getEnumConstants();

		if (enums.length <= ordinal) {
			throw new InvalidObjectException("No enum constant " + type.getCanonicalName() + " for ordinal " + ordinal);
		}

		return enums[ordinal];
	}

	@Override
	public <E extends Entity> E readEntity(Class<E> type) throws IOException {
		int id = readInt();

		if (id == -1) {
			return null;
		}

		E entity;

		try {
			entity = type.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new InvalidObjectException("Сan not read Entity by class " + type.getName() + " (" + e.toString() + " )");
		}

		if (entity._id() != id) {
			throw new InvalidObjectException(
				"Сan not read Entity by class " + type.getName() + " (Entity class id " + entity._id() + " differs from the received " + id + ")");
		}

		entity._decode(this);

		return entity;
	}

	@Override
	public byte[] readByteArray() throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		byte[] array = new byte[size];

		if (stream.read(array) == -1) {
			throw new EOFException();
		}

		return array;
	}

	@Override
	public boolean[] readBoolArray() throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		boolean[] array = new boolean[size];

		for (int i = 0; i < size; ++i) {
			array[i] = readBool();
		}

		return array;
	}

	@Override
	public int[] readIntArray() throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		int[] array = new int[size];

		for (int i = 0; i < size; ++i) {
			array[i] = readInt();
		}

		return array;
	}

	@Override
	public int[] readUintArray() throws IOException {
		return readIntArray();
	}

	@Override
	public long[] readLongArray() throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		long[] array = new long[size];

		for (int i = 0; i < size; ++i) {
			array[i] = readLong();
		}

		return array;
	}

	@Override
	public long[] readUlongArray() throws IOException {
		return readLongArray();
	}

	@Override
	public double[] readDoubleArray() throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		double[] array = new double[size];

		for (int i = 0; i < size; ++i) {
			array[i] = readDouble();
		}

		return array;
	}

	@Override
	public <E> E[] readArray(Decoder<E> elementDecoder) throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		E[] array = elementDecoder.createArray(size);

		for (int i = 0; i < size; ++i) {
			array[i] = elementDecoder.decode(this);
		}

		return array;
	}

	@Override
	public <K, V> Map<K, V> readMap(Decoder<K> keyDecoder, Decoder<V> valueDecoder) throws IOException {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		Map<K, V> map = new LinkedHashMap<>(size);

		for (int i = 0; i < size; ++i) {
			map.put(keyDecoder.decode(this), valueDecoder.decode(this));
		}

		return map;
	}

	private int read() throws IOException {
		int b = stream.read();
		if (b == -1) {
			throw new EOFException();
		}
		return b;
	}

	private long readRawLong() throws IOException {
		readBytesToBuffer(8);

		return (((long)buffer[0] << 56) +
			((long)(buffer[1] & 0xFF) << 48) +
			((long)(buffer[2] & 0xFF) << 40) +
			((long)(buffer[3] & 0xFF) << 32) +
			((long)(buffer[4] & 0xFF) << 24) +
			((buffer[5] & 0xFF) << 16) +
			((buffer[6] & 0xFF) << 8) +
			((buffer[7] & 0xFF)));
	}

	private void readBytesToBuffer(int len) throws IOException {
		int n = 0;
		while (n < len) {
			int count = stream.read(buffer, n, len - n);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}
}
