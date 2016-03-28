package org.shypl.biser.csi;

import org.apache.commons.lang3.ArrayUtils;
import org.shypl.common.util.BytesUtils;

public class ByteBuffer {
	protected byte array[];
	protected int  writerCursor;
	protected int  readerCursor;

	public ByteBuffer() {
		this(32);
	}

	public ByteBuffer(int capacity) {
		array = new byte[capacity];
	}

	public ByteBuffer writeByte(byte b) {
		increaseCapacity(1);
		array[writerCursor++] = b;
		return this;
	}

	public ByteBuffer writeBytes(byte... bytes) {
		return writeBytes(bytes, 0, bytes.length);
	}

	public ByteBuffer writeBytes(byte bytes[], int offset, int len) {
		increaseCapacity(len);
		System.arraycopy(bytes, offset, array, writerCursor, len);
		writerCursor += len;
		return this;
	}

	public ByteBuffer writeInt(int i) {
		increaseCapacity(4);
		BytesUtils.writeInt(array, i, writerCursor);
		writerCursor += 4;
		return this;
	}

	public ByteBuffer writeLong(long l) {
		increaseCapacity(8);
		BytesUtils.writeLong(array, l, writerCursor);
		writerCursor += 8;
		return this;
	}

	public int getReadableBytes() {
		return writerCursor - readerCursor;
	}

	public boolean isReadable() {
		return writerCursor > readerCursor;
	}

	public byte[] readBytes() {
		if (isReadable()) {
			byte[] result = new byte[getReadableBytes()];
			System.arraycopy(array, readerCursor, result, 0, result.length);
			readerCursor += result.length;
			return result;
		}
		else {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
	}

	public int readInt() {
		int i = BytesUtils.readInt(array, readerCursor);
		readerCursor += 4;
		return i;
	}

	public long readLong() {
		long l = BytesUtils.readLong(array, readerCursor);
		readerCursor += 8;
		return l;
	}

	public void clear() {
		writerCursor = 0;
		readerCursor = 0;
	}

	public int getCapacity() {
		return array.length;
	}

	private void increaseCapacity(int v) {
		int newCapacity = writerCursor + v;
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
			System.arraycopy(array, 0, newArray, 0, writerCursor);
			array = newArray;
		}
	}
}
