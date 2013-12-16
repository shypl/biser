package org.shypl.biser;

public class InputBuffer
{
	private final byte[] bytes;
	private int pos = 0;

	public InputBuffer(byte[] bytes)
	{
		this.bytes = bytes;
	}

	public boolean readBool()
	{
		return bytes[pos++] == 0x01;
	}

	public byte readByte()
	{
		return bytes[pos++];
	}

	public short readShort()
	{
		return (short)(
			(bytes[pos++] << 8) +
				(bytes[pos++] & 0xFF)
		);
	}

	public int readInt()
	{
		int b = bytes[pos++] & 0xFF;

		switch (b) {
			case 0xFF:
				return (bytes[pos++] << 24) +
					((bytes[pos++] & 0xFF) << 16) +
					((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFE:
				return -((bytes[pos++] & 0xFF) + 2);

			case 0xFD:
				return -1;

			case 0xFC:
				return ((bytes[pos++] & 0xFF) << 16) +
					((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFB:
				return ((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFA:
				return bytes[pos++] & 0xFF;

			default:
				return b;
		}
	}

	public long readUint()
	{
		int b = bytes[pos++] & 0xFF;

		switch (b) {
			case 0xFF:
				return ((long)bytes[pos++] << 24) +
					((bytes[pos++] & 0xFF) << 16) +
					((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFE:
				return ((bytes[pos++] & 0xFF) << 16) +
					((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFD:
				return ((bytes[pos++] & 0xFF) << 8) +
					(bytes[pos++] & 0xFF);

			case 0xFC:
				return bytes[pos++] & 0xFF;

			default:
				return b;
		}
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
					string[stringPos++] = (char)(((c & 0x0F) << 12) | ((bytes[pos++] & 0x3F) << 6) | (bytes[pos++] & 0x3F));
					break;
			}
		}

		return new String(string, 0, stringPos);
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

	public short[] readShortArray()
	{
		final int l = readInt();
		if (l == -1) {
			return null;
		}

		short[] a = new short[l];

		for (int i = 0; i < l; ++i) {
			a[i] = readShort();
		}

		return a;
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

	public byte[] readBytes()
	{
		return readByteArray();
	}
}
