package org.shypl.biser.io;

import java.io.IOException;

public class EnumDecoder<T extends Enum<T>> extends TypedDecoder<T> {

	private final T[] values;

	public EnumDecoder(Class<T> type) {
		super(type);
		this.values = type.getEnumConstants();
	}

	@Override
	public T decode(BiserReader reader) throws IOException {
		int ordinal = reader.readInt();
		return ordinal == -1 ? null : values[ordinal];
	}
}
