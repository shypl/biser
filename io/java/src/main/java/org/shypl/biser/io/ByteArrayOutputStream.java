package org.shypl.biser.io;

import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayOutputStream implements OutputStream {

	protected byte[] array;
	protected int    cursor;

	public ByteArrayOutputStream() {
		this(32);
	}

	public ByteArrayOutputStream(int capacity) {
		array = new byte[capacity];
	}

	public int size() {
		return cursor;
	}

	public boolean isEmpty() {
		return cursor == 0;
	}

	public boolean isNotEmpty() {
		return cursor != 0;
	}

	@Override
	public void write(byte b) {
		increaseCapacity(1);
		array[cursor++] = b;
	}

	@Override
	public void write(byte bytes[], int offset, int len) {
		increaseCapacity(len);
		System.arraycopy(bytes, offset, array, cursor, len);
		cursor += len;
	}

	public void clear() {
		cursor = 0;
	}

	public byte[] toArray() {
		if (cursor == 0) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		byte[] result = new byte[cursor];
		System.arraycopy(array, 0, result, 0, cursor);
		return result;
	}

	protected void increaseCapacity(int v) {
		int newCapacity = cursor + v;
		int capacity = array.length;

		if (newCapacity < 0) {
			throw new OutOfMemoryError();
		}

		//noinspection Duplicates
		if (capacity < newCapacity) {
			capacity += capacity >> 1;
			if (capacity < newCapacity) {
				capacity = newCapacity;
			}
			else if (capacity < 0) {
				capacity = Integer.MAX_VALUE;
			}
			byte[] newArray = new byte[capacity];
			System.arraycopy(array, 0, newArray, 0, cursor);
			array = newArray;
		}
	}
}
