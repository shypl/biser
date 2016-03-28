package org.shypl.biser.io;

public interface OutputStream {
	void write(byte b);

	void write(byte bytes[], int offset, int len);

	default void write(byte bytes[]) {
		write(bytes, 0, bytes.length);
	}
}
