package org.shypl.biser;

public class InputBuffer
{
	private final byte[] bytes;
	private int pos = 0;

	public InputBuffer(final byte[] bytes)
	{
		this.bytes = bytes;
	}

	public boolean readBool()
	{
		return bytes[pos++] == 0x01;
	}

	public boolean[] readBoolArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		boolean[] a = new boolean[l];

		for (int i = 0; i < l; ++i) {
			a[i] = bytes[pos++] == 0x01;
		}

		return a;
	}

	public byte readByte()
	{
		return bytes[pos++];
	}

	public byte[] readByteArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		byte[] a = new byte[l];

		System.arraycopy(bytes, pos, a, 0, l);

		pos += l;

		return a;
	}

	public byte[] readBytes()
	{
		return readByteArray();
	}

	public int readInt()
	{
		int b = bytes[pos++] & 0xFF;

		switch (b) {
			case 0xFD:
				return -1;

			case 0xFA:
				return bytes[pos++] & 0xFF;

			case 0xFB:
				return ((bytes[pos++] & 0xFF) << 8) + (bytes[pos++] & 0xFF);

			case 0xFC:
				return ((bytes[pos++] & 0xFF) << 16) + ((bytes[pos++] & 0xFF) << 8) + (bytes[pos++] & 0xFF);

			case 0xFF:
				return (bytes[pos++] << 24) + ((bytes[pos++] & 0xFF) << 16) + ((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFE:
				return -((bytes[pos++] & 0xFF) + 2);

			default:
				return b;
		}
	}

	public int[] readIntArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		int[] a = new int[l];

		for (int i = 0; i < l; ++i) {
			a[i] = readInt();
		}

		return a;
	}

	public long readUint()
	{
		int b = bytes[pos++] & 0xFF;

		switch (b) {
			case 0xFF:
				return ((long)bytes[pos++] << 24)
					+ ((bytes[pos++] & 0xFF) << 16)
					+ ((bytes[pos++] & 0xFF) << 8)
					+ (bytes[pos++] & 0xFF);

			case 0xFE:
				return ((bytes[pos++] & 0xFF) << 16)
					+ ((bytes[pos++] & 0xFF) << 8)
					+ (bytes[pos++] & 0xFF);

			case 0xFD:
				return ((bytes[pos++] & 0xFF) << 8)
					+ (bytes[pos++] & 0xFF);

			case 0xFC:
				return bytes[pos++] & 0xFF;

			default:
				return b;
		}
	}

	public long[] readUintArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		long[] a = new long[l];

		for (int i = 0; i < l; ++i) {
			a[i] = readUint();
		}

		return a;
	}

	public long readNum()
	{
		return (long)readDouble();
	}

	public long[] readNumArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		long[] a = new long[l];

		for (int i = 0; i < l; ++i) {
			a[i] = readNum();
		}

		return a;
	}

	public double readDouble()
	{
		return Double.longBitsToDouble(((long)bytes[pos++] << 56) +
			((long)(bytes[pos++] & 0xFF) << 48) +
			((long)(bytes[pos++] & 0xFF) << 40) +
			((long)(bytes[pos++] & 0xFF) << 32) +
			((long)(bytes[pos++] & 0xFF) << 24) +
			((bytes[pos++] & 0xFF) << 16) +
			((bytes[pos++] & 0xFF) << 8) +
			(bytes[pos++] & 0xFF));
	}

	public double[] readDoubleArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		double[] a = new double[l];

		for (int i = 0; i < l; ++i) {
			a[i] = readDouble();
		}

		return a;
	}

	public String readString()
	{
		final int size = readInt();

		if (size == -1) {
			return null;
		}

		int i = 0;
		int stringPos = 0;
		int c;
		final char[] string = new char[size];

		while (i < size) {
			c = (bytes[pos++] & 0xFF);
			switch (c >> 4) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					++i;
					string[stringPos++] = (char)c;
					break;

				case 12:
				case 13:
					i += 2;
					string[stringPos++] = (char)(((c & 0x1F) << 6) | (bytes[pos++] & 0x3F));
					break;

				default:
					i += 3;
					string[stringPos++] = (char)(((c & 0x0F) << 12) | ((bytes[pos++] & 0x3F) << 6) | (bytes[pos++]
						& 0x3F));
					break;
			}
		}

		return new String(string, 0, stringPos);
	}

	public String[] readStringArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		String[] a = new String[l];

		for (int i = 0; i < l; ++i) {
			a[i] = readString();
		}

		return a;
	}
}
