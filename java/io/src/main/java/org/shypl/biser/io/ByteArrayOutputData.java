package org.shypl.biser.io;

public class ByteArrayOutputData {

	private final ByteArrayOutputStream stream;
	private final DataWriter            writer;

	public ByteArrayOutputData() {
		stream = new ByteArrayOutputStream();
		writer = new DataWriter(stream);
	}

	public DataWriter getWriter() {
		return writer;
	}

	public boolean isNotEmpty() {
		return stream.isNotEmpty();
	}

	public void clear() {
		stream.clear();
	}

	public byte[] getArray() {
		return stream.toArray();
	}
}
