package org.shypl.biser.io;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

public class DataWriter {
	private final OutputStream stream;
	private byte[] buffer = new byte[9];

	public DataWriter(OutputStream stream) {
		this.stream = stream;
	}

	public void writeByte(byte value) {
		stream.write(value);
	}

	public void writeBool(boolean value) {
		stream.write((byte)(value ? 0x01 : 0x00));
	}

	public void writeInt(int value) {
		if (value == -1) {
			stream.write((byte)0xFF);
		}
		else if (value >= 0 && value <= 253) {
			stream.write((byte)value);
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

	public void writeUint(long value) {
		if (value < 0 || value > 4294967295L) {
			throw new IllegalArgumentException("Value " + value + " out of bounds for uint");
		}

		if (value >= 0 && value <= 254) {
			stream.write((byte)value);
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

	public void writeLong(long value) {
		if (value == -1) {
			stream.write((byte)0xFF);
		}
		else if (value >= 0 && value <= 253) {
			stream.write((byte)value);
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

	public void writeUlong(long value) {
		if (value >= 0 && value <= 254) {
			stream.write((byte)value);
		}
		else {
			buffer[0] = (byte)0xFF;
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

	public void writeDouble(double value) {
		writeRawLong(Double.doubleToLongBits(value));
	}

	public void writeBytes(byte[] value) {
		writeByteArray(value);
	}

	public void writeString(String value) {
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

	public void writeEnum(Enum<?> value) {
		writeInt(value == null ? -1 : value.ordinal());
	}

	public void writeEntity(Entity value) {
		if (value == null) {
			writeInt(-1);
		}
		else {
			writeInt(value._id());
			value._encode(this);
		}
	}

	public void writeByteArray(byte[] array) {
		if (array == null) {
			writeInt(-1);
		}
		else {
			writeInt(array.length);
			stream.write(array);
		}
	}

	public void writeBoolArray(boolean[] array) {
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

	public void writeIntArray(int[] array) {
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

	public void writeUintArray(int[] array) {
		writeIntArray(array);
	}

	public void writeLongArray(long[] array) {
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

	public void writeUlongArray(long[] array) {
		writeLongArray(array);
	}

	public void writeDoubleArray(double[] array) {
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

	public <E> void writeArray(E[] array, Encoder<E> elementEncoder) {
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

	public <K, V> void writeMap(Map<K, V> map, Encoder<? super K> keyEncoder, Encoder<? super V> valueEncoder) {
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

	public <E> void writeCollection(Collection<E> collection, Encoder<? super E> elementEncoder) {
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

	private void writeRawLong(long value) {
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
