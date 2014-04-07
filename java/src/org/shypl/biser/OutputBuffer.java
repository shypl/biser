package org.shypl.biser;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("Convert2streamapi")
public class OutputBuffer
{
	private int pos = 0;
	private int    capacity;
	private byte[] bytes;

	public OutputBuffer()
	{
		this(10);
	}

	public OutputBuffer(final int capacity)
	{
		this.capacity = capacity;
		bytes = new byte[capacity];
	}

	public byte[] bytes()
	{
		return pos == 0 ? new byte[0] : Arrays.copyOf(bytes, pos);
	}

	public void clear()
	{
		clearTo(0);
	}

	public void clearTo(final int length)
	{
		pos = length;
	}

	public int size()
	{
		return pos;
	}

	public void writeBool(final boolean v)
	{
		increaseCapacity(1);
		bytes[pos++] = (byte)(v ? 0x01 : 0x00);
	}

	public void writeBoolArray(final boolean[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			increaseCapacity(v.length);
			for (boolean e : v) {
				bytes[pos++] = (byte)(e ? 1 : 0);
			}
		}
	}

	public void writeBoolArray(final Collection<Boolean> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			increaseCapacity(v.size());
			for (boolean e : v) {
				bytes[pos++] = (byte)(e ? 1 : 0);
			}
		}
	}

	public void writeByte(final byte v)
	{
		increaseCapacity(1);
		bytes[pos++] = v;
	}

	public void writeByteArray(final byte[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			increaseCapacity(v.length);
			System.arraycopy(v, 0, bytes, pos, v.length);
			pos += v.length;
		}
	}

	public void writeByteArray(final Collection<Byte> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			increaseCapacity(v.size());
			for (byte e : v) {
				writeByte(e);
			}
		}
	}

	public void writeBytes(final byte[] v)
	{
		writeByteArray(v);
	}

	public void writeDouble(final double v)
	{
		long l = Double.doubleToLongBits(v);

		increaseCapacity(8);
		bytes[pos++] = (byte)(l >>> 56);
		bytes[pos++] = (byte)(l >>> 48);
		bytes[pos++] = (byte)(l >>> 40);
		bytes[pos++] = (byte)(l >>> 32);
		bytes[pos++] = (byte)(l >>> 24);
		bytes[pos++] = (byte)(l >>> 16);
		bytes[pos++] = (byte)(l >>> 8);
		bytes[pos++] = (byte)l;
	}

	public void writeDoubleArray(final double[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			for (double e : v) {
				writeDouble(e);
			}
		}
	}

	public void writeDoubleArray(final Collection<Double> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			for (double e : v) {
				writeDouble(e);
			}
		}
	}

	public void writeEntity(final BiserEntity v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v._eid());
			v.encode(this);
		}
	}

	public void writeEntityArray(final BiserEntity[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			for (BiserEntity e : v) {
				writeEntity(e);
			}
		}
	}

	public void writeEntityArray(final Collection<BiserEntity> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			for (BiserEntity e : v) {
				writeEntity(e);
			}
		}
	}

	public void writeEnum(final Enum v)
	{
		writeInt(v == null ? -1 : v.ordinal());
	}

	public void writeEnumArray(final Enum[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			for (Enum e : v) {
				writeEnum(e);
			}
		}
	}

	public void writeEnumArray(final Collection<Enum> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			for (Enum e : v) {
				writeEnum(e);
			}
		}
	}

	public void writeInt(final int v)
	{
		if (v >= 0) {

			if (v <= 0xF9) {
				increaseCapacity(1);
				bytes[pos++] = (byte)v;
				return;
			}

			if (v <= 0xFF) {
				increaseCapacity(2);
				bytes[pos++] = (byte)0xFA;
				bytes[pos++] = (byte)v;
				return;
			}

			if (v <= 0xFFFF) {
				increaseCapacity(3);
				bytes[pos++] = (byte)0xFB;
				bytes[pos++] = (byte)(v >>> 8);
				bytes[pos++] = (byte)v;
				return;
			}

			if (v <= 0xFFFFFF) {
				increaseCapacity(4);
				bytes[pos++] = (byte)0xFC;
				bytes[pos++] = (byte)(v >>> 16);
				bytes[pos++] = (byte)(v >>> 8);
				bytes[pos++] = (byte)v;
				return;
			}
		}
		else if (v == -1) {
			increaseCapacity(1);
			bytes[pos++] = (byte)0xFD;
			return;
		}
		else if (v >= -257) {
			increaseCapacity(2);
			bytes[pos++] = (byte)0xFE;
			bytes[pos++] = (byte)(-v - 2);
			return;
		}

		increaseCapacity(5);
		bytes[pos++] = (byte)0xFF;
		bytes[pos++] = (byte)(v >>> 24);
		bytes[pos++] = (byte)(v >>> 16);
		bytes[pos++] = (byte)(v >>> 8);
		bytes[pos++] = (byte)v;
	}

	public void writeIntArray(final int[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			for (int e : v) {
				writeInt(e);
			}
		}
	}

	public void writeIntArray(final Collection<Integer> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			for (int e : v) {
				writeInt(e);
			}
		}
	}

	public void writeString(final String v)
	{
		if (v == null) {
			writeInt(-1);
			return;
		}

		int strLen = v.length();

		if (strLen == 0) {
			writeInt(0);
			return;
		}

		int utfLen = 0;
		int i;
		char c;

		for (i = 0; i < strLen; i++) {
			c = v.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utfLen++;
			}
			else if (c > 0x07FF) {
				utfLen += 3;
			}
			else {
				utfLen += 2;
			}
		}

		writeInt(utfLen);
		increaseCapacity(utfLen);

		for (i = 0; i < strLen; i++) {
			c = v.charAt(i);
			if (c >= 0x0001 && c <= 0x007F) {
				bytes[pos++] = (byte)c;
			}
			else if (c > 0x07FF) {
				bytes[pos++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
				bytes[pos++] = (byte)(0x80 | ((c >> 6) & 0x3F));
				bytes[pos++] = (byte)(0x80 | (c & 0x3F));
			}
			else {
				bytes[pos++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
				bytes[pos++] = (byte)(0x80 | (c & 0x3F));
			}
		}
	}

	public void writeStringArray(final String[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			for (String e : v) {
				writeString(e);
			}
		}
	}

	public void writeStringArray(final Collection<String> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			for (String e : v) {
				writeString(e);
			}
		}
	}

	public void writeUint(final long v)
	{
		if (v < 0 || v > 0xFFFFFFFFL) {
			throw new IllegalArgumentException();
		}

		if (v <= 0xFB) {
			increaseCapacity(1);
			bytes[pos++] = (byte)v;
			return;
		}

		if (v <= 0xFF) {
			increaseCapacity(2);
			bytes[pos++] = (byte)0xFC;
			bytes[pos++] = (byte)v;
			return;
		}

		if (v <= 0xFFFF) {
			increaseCapacity(3);
			bytes[pos++] = (byte)0xFD;
			bytes[pos++] = (byte)(v >>> 8);
			bytes[pos++] = (byte)v;
			return;
		}

		if (v <= 0xFFFFFF) {
			increaseCapacity(4);
			bytes[pos++] = (byte)0xFE;
			bytes[pos++] = (byte)(v >>> 16);
			bytes[pos++] = (byte)(v >>> 8);
			bytes[pos++] = (byte)v;
			return;
		}

		increaseCapacity(5);
		bytes[pos++] = (byte)0xFF;
		bytes[pos++] = (byte)(v >>> 24);
		bytes[pos++] = (byte)(v >>> 16);
		bytes[pos++] = (byte)(v >>> 8);
		bytes[pos++] = (byte)v;
	}

	public void writeUintArray(final long[] v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.length);
			for (long e : v) {
				writeUint(e);
			}
		}
	}

	public void writeUintArray(final Collection<Long> v)
	{
		if (v == null) {
			writeInt(-1);
		}
		else {
			writeInt(v.size());
			for (Long e : v) {
				writeUint(e);
			}
		}
	}

	private void increaseCapacity(final int v)
	{
		int cap = pos + v;

		if (cap < 0) {
			throw new OutOfMemoryError();
		}
		else if (capacity < cap) {
			capacity += capacity >> 1;
			if (capacity < cap) {
				capacity = cap;
			}
			if (capacity < 0) {
				throw new OutOfMemoryError();
			}
			bytes = Arrays.copyOf(bytes, capacity);
		}
	}
}
