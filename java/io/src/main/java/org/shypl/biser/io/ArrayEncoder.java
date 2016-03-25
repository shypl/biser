package org.shypl.biser.io;

class ArrayEncoder<T> implements Encoder<T[]> {
	private final Encoder<T> elementEncoder;

	public ArrayEncoder(Encoder<T> elementEncoder) {
		this.elementEncoder = elementEncoder;
	}

	@Override
	public void encode(T[] value, DataWriter writer) {
		writer.writeArray(value, elementEncoder);
	}
}
