package org.shypl.biser.io;

import java.io.InputStream;

public class ByteArrayInputStream extends InputStream {

	protected byte[] buffer;

	protected int position;

	public ByteArrayInputStream(byte buffer[]) {
		this.buffer = buffer;
		this.position = 0;
	}

	@Override
	public int read() {
		return (position < buffer.length) ? (buffer[position++] & 0xff) : -1;
	}

	@Override
	public synchronized int read(byte b[], int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		}

		if (position >= buffer.length) {
			return -1;
		}

		int avail = buffer.length - position;
		if (len > avail) {
			len = avail;
		}
		if (len <= 0) {
			return 0;
		}
		System.arraycopy(buffer, position, b, off, len);
		position += len;
		return len;
	}

	@Override
	public long skip(long n) {
		long k = buffer.length - position;
		if (n < k) {
			k = n < 0 ? 0 : n;
		}

		position += k;
		return k;
	}

	@Override
	public int available() {
		return buffer.length - position;
	}

	@Override
	public void close() {
	}
}
