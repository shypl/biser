package org.shypl.biser.csi;

import org.shypl.common.util.ByteUtils;

import java.util.Arrays;

public class ByteArrayBuilder {
	protected byte buf[];

	protected int size;

	public ByteArrayBuilder() {
		this(32);
	}

	public ByteArrayBuilder(int capacity) {
		buf = new byte[capacity];
	}

	public ByteArrayBuilder add(byte b) {
		increaseCapacity(1);
		buf[size++] = b;
		return this;
	}

	public ByteArrayBuilder add(byte... bytes) {
		return add(bytes, 0, bytes.length);
	}

	public ByteArrayBuilder add(byte bytes[], int off, int len) {
		if (len == 0) {
			return this;
		}

		int bLen = bytes.length;
		int offAndLen = off + len;

		if ((off < 0) || (off > bLen) || (len < 0) || (offAndLen > bLen) || (offAndLen < 0)) {
			throw new IndexOutOfBoundsException();
		}

		increaseCapacity(len);
		System.arraycopy(bytes, off, buf, size, len);

		size += len;
		return this;
	}

	public ByteArrayBuilder add(int i) {
		increaseCapacity(4);
		ByteUtils.writeInt(buf, i, size);
		size += 4;
		return this;
	}

	public ByteArrayBuilder add(long l) {
		increaseCapacity(8);
		ByteUtils.writeLong(buf, l, size);
		size += 8;
		return this;
	}

	public byte[] build() {
		return Arrays.copyOf(buf, size);
	}

	private void increaseCapacity(int v) {
		int newCapacity = size + v;
		int oldCapacity = buf.length;

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
			buf = Arrays.copyOf(buf, oldCapacity);
		}
	}
}
