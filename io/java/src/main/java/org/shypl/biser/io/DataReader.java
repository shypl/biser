package org.shypl.biser.io;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataReader {
	private final InputStream stream;
	private byte[] buffer = new byte[8];

	public DataReader(InputStream stream) {
		this.stream = stream;
	}

	public byte readByte() {
		return stream.read();
	}

	public boolean readBool() {
		return readByte() == 0x01;
	}

	public int readInt() {
		int b = readByte() & 0xFF;

		switch (b) {
			case 0xFF:
				return -1;
			case 0xFE:
				return readRawInt();
			default:
				return b;
		}
	}

	public int readUint() {
		int b = readByte() & 0xFF;

		switch (b) {
			case 0xFF:
				return readRawInt();
			default:
				return b;
		}
	}

	public long readLong() {
		int b = readByte() & 0xFF;

		switch (b) {
			case 0xFF:
				return -1;
			case 0xFE:
				return readRawLong();
			default:
				return b;
		}
	}

	public long readUlong() {
		int b = readByte() & 0xFF;

		switch (b) {
			case 0xFF:
				return readRawLong();
			default:
				return b;
		}
	}

	public double readDouble() {
		return Double.longBitsToDouble(readRawLong());
	}

	public byte[] readBytes() {
		return readByteArray();
	}

	public String readString() {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		if (size == 0) {
			return StringUtils.EMPTY;
		}

		if (buffer.length < size) {
			buffer = new byte[size];
		}

		readBytesToBuffer(size);

		return new String(buffer, 0, size, StandardCharsets.UTF_8);
	}

	public Date readDate() {
		long ms = readRawLong();

		if (ms == Long.MIN_VALUE) {
			return null;
		}

		return new Date(ms);
	}

	public <E extends Enum<E>> E readEnum(Class<E> type) {
		int ordinal = readInt();

		if (ordinal == -1) {
			return null;
		}

		E[] enums = type.getEnumConstants();

		if (enums.length <= ordinal) {
			throw new RuntimeException("No enum constant " + type.getCanonicalName() + " for ordinal " + ordinal);
		}

		return enums[ordinal];
	}

	public <E extends Entity> E readEntity(Class<E> type) {
		int id = readInt();

		if (id == -1) {
			return null;
		}

		E entity;

		try {
			entity = type.newInstance();
		}
		catch (ReflectiveOperationException e) {
			throw new RuntimeException("Сan not read Entity by class " + type.getName(), e);
		}

		if (entity.getBiserEntityClassId() != id) {
			throw new RuntimeException(
				"Сan not read Entity by class " + type.getName() + " (Entity class id " + entity.getBiserEntityClassId() + " differs from the received " + id + ")");
		}

		entity._decode(this);

		return entity;
	}

	public byte[] readByteArray() {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		byte[] array = new byte[size];

		stream.read(array);

		return array;
	}

	public boolean[] readBoolArray() {
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

	public int[] readIntArray() {
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

	public int[] readUintArray() {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		int[] array = new int[size];

		for (int i = 0; i < size; ++i) {
			array[i] = readUint();
		}

		return array;
	}

	public long[] readLongArray() {
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

	public long[] readUlongArray() {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		long[] array = new long[size];

		for (int i = 0; i < size; ++i) {
			array[i] = readUlong();
		}

		return array;
	}

	public double[] readDoubleArray() {
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

	@SuppressWarnings("unchecked")
	public <E> E[] readArray(Decoder<? super E> elementDecoder) {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		E[] array = (E[])elementDecoder.createArray(size);

		for (int i = 0; i < size; ++i) {
			array[i] = (E)elementDecoder.decode(this);
		}

		return array;
	}

	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> readMap(Decoder<? super K> keyDecoder, Decoder<? super V> valueDecoder) {
		int size = readInt();

		if (size == -1) {
			return null;
		}

		Map<K, V> map = new LinkedHashMap<>(size);

		for (int i = 0; i < size; ++i) {
			map.put((K)keyDecoder.decode(this), (V)valueDecoder.decode(this));
		}

		return map;
	}

	public <E> List<E> readCollection(Decoder<? super E> elementDecoder) {
		E[] array = (E[])readArray(elementDecoder);
		return Arrays.asList(array);
	}

	private int readRawInt() {
		readBytesToBuffer(4);

		return ((buffer[0] & 0xFF) << 24) +
			((buffer[1] & 0xFF) << 16) +
			((buffer[2] & 0xFF) << 8) +
			(buffer[3] & 0xFF);
	}

	private long readRawLong() {
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

	private void readBytesToBuffer(int len) {
		stream.read(buffer, 0, len);
	}
}
