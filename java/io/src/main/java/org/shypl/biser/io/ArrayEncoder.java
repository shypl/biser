package org.shypl.biser.io;

import java.io.IOException;

class ArrayEncoder<T> implements Encoder<T[]> {
	private final Encoder<T> elementEncoder;

	public ArrayEncoder(Encoder<T> elementEncoder) {
		this.elementEncoder = elementEncoder;
	}

	@Override
	public void encode(T[] value, BiserWriter writer) throws IOException {
		writer.writeArray(value, elementEncoder);
	}
}
