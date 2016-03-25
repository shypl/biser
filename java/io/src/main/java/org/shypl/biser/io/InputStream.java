package org.shypl.biser.io;

public interface InputStream {
	int getReadableBytes();

	boolean isReadable();

	default boolean isReadable(int size) {
		return getReadableBytes() >= size;
	}

	byte read();

	void read(byte[] target, int targetOffset, int len);

	default void read(byte[] target) {
		read(target, 0, target.length);
	}

	default int read(byte[] target, int targetOffset) {
		int len = Math.min(target.length - targetOffset, getReadableBytes());
		read(target, targetOffset, len);
		return len;
	}
}
