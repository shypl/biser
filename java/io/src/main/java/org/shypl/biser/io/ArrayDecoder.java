package org.shypl.biser.io;

class ArrayDecoder<T> extends TypedDecoder<T[]> {

	@SuppressWarnings("unchecked")
	public static <T> Decoder<T[]> factory(Decoder<T> elementDecoder) {
		return new ArrayDecoder<>((Class<T[]>)elementDecoder.createArray(0).getClass(), elementDecoder);
	}

	private Decoder<T> elementDecoder;

	public ArrayDecoder(Class<T[]> type, Decoder<T> elementDecoder) {
		super(type);
		this.elementDecoder = elementDecoder;
	}

	@Override
	public T[] decode(DataReader reader) {
		return reader.readArray(elementDecoder);
	}
}
