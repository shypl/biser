package org.shypl.biser.server;

class ByteBuffer
{
	private final byte[] bytes;
	private int position = 0;

	public ByteBuffer(final byte[] bytes)
	{
		this.bytes = bytes;
	}

	public int size()
	{
		return bytes.length - position;
	}

	public boolean isReadable()
	{
		return position < bytes.length;
	}

	public byte readByte()
	{
		return bytes[position++];
	}

	public void readBytes(final byte[] target, final int offset, final int length)
	{
		System.arraycopy(bytes, position, target, offset, length);
	}
}
