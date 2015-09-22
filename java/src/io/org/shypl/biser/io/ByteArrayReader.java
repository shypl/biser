package org.shypl.biser.io;

public class ByteArrayReader extends SafeReaderProxy {
	public ByteArrayReader(byte[] bytes) {
		super(new StreamReader(new ByteArrayInputStream(bytes)));
	}
}
