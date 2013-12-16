package org.shypl.biser;

public abstract class DataObject
{
	public byte[] encode()
	{
		final OutputBuffer buffer = new OutputBuffer();
		encode(buffer);
		return buffer.bytes();
	}

	public void decode(byte[] bytes)
	{
		decode(new InputBuffer(bytes));
	}

	public abstract void encode(final OutputBuffer b);

	public abstract void decode(final InputBuffer b);

	@Override
	public String toString()
	{
		return Utils.representObject(this);
	}
}
