package org.shypl.biser.io;

import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayInputData {
	private final ByteArrayInputStream stream;
	private final DataReader           reader;

	public ByteArrayInputData() {
		this(ArrayUtils.EMPTY_BYTE_ARRAY);
	}

	public ByteArrayInputData(byte[] array) {
		stream = new ByteArrayInputStream(array);
		reader = new DataReader(stream);
	}

	public DataReader getReader() {
		return reader;
	}

	public void reset() {
		stream.reset();
	}

	public void reset(byte array[]) {
		stream.reset(array);
	}
}
