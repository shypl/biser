package org.shypl.biser.io;

import java.io.OutputStream;
import java.util.Arrays;

public class ByteArrayOutputStream extends OutputStream {

	protected byte[] buffer;

	protected int size;

	public ByteArrayOutputStream() {
		this(32);
	}

	public ByteArrayOutputStream(int capacity) {
		buffer = new byte[capacity];
	}

	@Override
	public void write(int b) {
		increaseCapacity(1);
		buffer[size++] = (byte)b;
	}

	@Override
	public void write(byte b[], int off, int len) {
		if (len == 0) {
			return;
		}

		int bLen = b.length;
		int offAndLen = off + len;

		if ((off < 0) || (off > bLen) || (len < 0) || (offAndLen > bLen) || (offAndLen < 0)) {
			throw new IndexOutOfBoundsException();
		}

		increaseCapacity(len);
		System.arraycopy(b, off, buffer, size, len);

		size += len;
	}

	public byte[] toByteArray() {
		return Arrays.copyOf(buffer, size);
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean isNotEmpty() {
		return size != 0;
	}

	private void increaseCapacity(int v) {
		int newCapacity = size + v;
		int oldCapacity = buffer.length;

		if (newCapacity < 0) {
			throw new OutOfMemoryError();
		}

		if (oldCapacity < newCapacity) {
			oldCapacity += oldCapacity >> 1;
			if (oldCapacity < newCapacity) {
				oldCapacity = newCapacity;
			}
			if (oldCapacity < 0) {
				throw new OutOfMemoryError();
			}
			buffer = Arrays.copyOf(buffer, oldCapacity);
		}
	}
}
