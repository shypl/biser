package org.shypl.biser.io;


public class ByteArrayInputStream implements InputStream {

	protected byte[] array;
	protected int    cursor;

	public ByteArrayInputStream(byte array[]) {
		this.array = array;
	}

	@Override
	public int getReadableBytes() {
		return array.length - cursor;
	}

	@Override
	public boolean isReadable() {
		return cursor < array.length;
	}

	@Override
	public byte read() {
		return array[cursor++];
	}

	@Override
	public void read(byte[] target, int targetOffset, int len) {
		System.arraycopy(array, cursor, target, targetOffset, len);
		cursor += len;
	}

	public void reset() {
		cursor = 0;
	}

	public void reset(byte array[]) {
		this.array = array;
		cursor = 0;
	}
}
