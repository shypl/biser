package org.shypl.biser.io;

import java.lang.reflect.Array;

public abstract class TypedDecoder<T> implements Decoder<T> {
	protected final Class<T> type;

	public TypedDecoder(Class<T> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] createArray(int length) {
		return (T[])Array.newInstance(type, length);
	}
}
