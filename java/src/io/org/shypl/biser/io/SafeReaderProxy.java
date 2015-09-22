package org.shypl.biser.io;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class SafeReaderProxy implements BiserReader {
	protected final BiserReader reader;

	public SafeReaderProxy(BiserReader reader) {
		this.reader = reader;
	}

	@Override
	public byte readByte() {
		try {
			return reader.readByte();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean readBool() {
		try {
			return reader.readBool();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int readInt() {
		try {
			return reader.readInt();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int readUint() {
		try {
			return reader.readUint();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long readLong() {
		try {
			return reader.readLong();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long readUlong() {
		try {
			return reader.readUlong();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double readDouble() {
		try {
			return reader.readDouble();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] readBytes() {
		try {
			return reader.readBytes();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String readString() {
		try {
			return reader.readString();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Date readDate() {
		try {
			return reader.readDate();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E extends Enum<E>> E readEnum(Class<E> type) {
		try {
			return reader.readEnum(type);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E extends Entity> E readEntity(Class<E> type) {
		try {
			return reader.readEntity(type);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] readByteArray() {
		try {
			return reader.readByteArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean[] readBoolArray() {
		try {
			return reader.readBoolArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[] readIntArray() {
		try {
			return reader.readIntArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[] readUintArray() {
		try {
			return reader.readUintArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long[] readLongArray() {
		try {
			return reader.readLongArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long[] readUlongArray() {
		try {
			return reader.readUlongArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double[] readDoubleArray() {
		try {
			return reader.readDoubleArray();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E> E[] readArray(Decoder<E> elementDecoder) {
		try {
			return reader.readArray(elementDecoder);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <K, V> Map<K, V> readMap(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
		try {
			return reader.readMap(keyDecoder, valueDecoder);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
