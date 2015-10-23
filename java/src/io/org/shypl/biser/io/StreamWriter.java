package org.shypl.biser.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class StreamWriter implements BiserWriter {
	protected final OutputStream stream;
	private byte[] buffer = new byte[9];

	public StreamWriter(OutputStream stream) {
		this.stream = stream;
	}

	@Override
	public void writeByte(byte value) throws IOException {
		stream.write(value);
	}

	@Override
	public void writeBool(boolean value) throws IOException {
		stream.write(value ? 0x01 : 0x00);
	}

	@Override
	public void writeInt(int value) throws IOException {
		if (value == -1) {
			stream.write(0xFF);
		}
		else if (value >= 0 && value <= 253) {
			stream.write(value);
		}
		else {
			buffer[0] = (byte)0xFE;
			buffer[1] = (byte)(value >>> 24);
			buffer[2] = (byte)(value >>> 16);
			buffer[3] = (byte)(value >>> 8);
			buffer[4] = (byte)value;
			stream.write(buffer, 0, 5);
		}
	}

	@Override
	public void writeUint(long value) throws IOException {
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new IllegalArgumentException();
		}

		if (value >= 0 && value <= 254) {
			stream.write((int)value);
		}
		else {
			buffer[0] = (byte)0xFF;
			buffer[1] = (byte)(value >>> 24);
			buffer[2] = (byte)(value >>> 16);
			buffer[3] = (byte)(value >>> 8);
			buffer[4] = (byte)value;
			stream.write(buffer, 0, 5);
		}
	}

	@Override
	public void writeLong(long value) throws IOException {
		if (value == -1) {
			stream.write(0xFF);
		}
		else if (value >= 0 && value <= 253) {
			stream.write((int)value);
		}
		else {
			buffer[0] = (byte)0xFE;
			buffer[1] = (byte)(value >>> 56);
			buffer[2] = (byte)(value >>> 48);
			buffer[3] = (byte)(value >>> 40);
			buffer[4] = (byte)(value >>> 32);
			buffer[5] = (byte)(value >>> 24);
			buffer[6] = (byte)(value >>> 16);
			buffer[7] = (byte)(value >>> 8);
			buffer[8] = (byte)value;
			stream.write(buffer, 0, 9);
		}
	}

	@Override
	public void writeUlong(long value) throws IOException {
		writeLong(value);
	}

	@Override
	public void writeDouble(double value) throws IOException {
		writeRawLong(Double.doubleToLongBits(value));
	}

	@Override
	public void writeBytes(byte[] value) throws IOException {
		writeByteArray(value);
	}

	@Override
	public void writeString(String value) throws IOException {
		if (value == null) {
			writeInt(-1);
		}
		else if (value.length() == 0) {
			writeInt(0);
		}
		else {
			byte[] utfBytes = value.getBytes(StandardCharsets.UTF_8);
			writeInt(utfBytes.length);
			stream.write(utfBytes);
		}
	}

	@Override
	public void writeDate(Date value) throws IOException {
		writeRawLong(value == null ? Long.MIN_VALUE : value.getTime());
	}

	@Override
	public void writeEnum(Enum<?> value) throws IOException {
		writeInt(value == null ? -1 : value.ordinal());
	}

	@Override
	public void writeEntity(Entity value) throws IOException {
		if (value == null) {
			writeInt(-1);
		}
		else {
			writeInt(value._id());
			value._encode(this);
		}
	}

	@Override
	public void writeByteArray(byte[] array) throws IOException {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			stream.write(array);
		}
	}

	@Override
	public void writeBoolArray(boolean[] array) throws IOException {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			for (boolean value : array) {
				writeBool(value);
			}
		}
	}

	@Override
	public void writeIntArray(int[] array) throws IOException {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			for (int value : array) {
				writeInt(value);
			}
		}
	}

	@Override
	public void writeUintArray(int[] array) throws IOException {
		writeIntArray(array);
	}

	@Override
	public void writeLongArray(long[] array) throws IOException {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			for (long value : array) {
				writeLong(value);
			}
		}
	}

	@Override
	public void writeUlongArray(long[] array) throws IOException {
		writeLongArray(array);
	}

	@Override
	public void writeDoubleArray(double[] array) throws IOException {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			for (double value : array) {
				writeDouble(value);
			}
		}
	}

	@Override
	public <E> void writeArray(E[] array, Encoder<E> elementEncoder) throws IOException {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			for (E value : array) {
				elementEncoder.encode(value, this);
			}
		}
	}

	@Override
	public <K, V> void writeMap(Map<K, V> map, Encoder<? super K> keyEncoder, Encoder<? super V> valueEncoder) throws IOException {
		if (map == null) {
			writeInt(-1);
		}
		else {
			writeInt(map.size());
			for (Map.Entry<K, V> entry : map.entrySet()) {
				keyEncoder.encode(entry.getKey(), this);
				valueEncoder.encode(entry.getValue(), this);
			}
		}
	}

	@Override
	public <E> void writeCollection(Collection<E> collection, Encoder<? super E> elementEncoder) throws IOException {
		if (collection == null) {
			writeInt(-1);
		}
		else {
			writeInt(collection.size());
			for (E value : collection) {
				elementEncoder.encode(value, this);
			}
		}
	}

	private void writeRawLong(long value) throws IOException {
		buffer[0] = (byte)(value >>> 56);
		buffer[1] = (byte)(value >>> 48);
		buffer[2] = (byte)(value >>> 40);
		buffer[3] = (byte)(value >>> 32);
		buffer[4] = (byte)(value >>> 24);
		buffer[5] = (byte)(value >>> 16);
		buffer[6] = (byte)(value >>> 8);
		buffer[7] = (byte)value;
		stream.write(buffer, 0, 8);
	}
}
