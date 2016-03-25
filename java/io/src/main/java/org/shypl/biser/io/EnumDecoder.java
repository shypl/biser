package org.shypl.biser.io;

public class EnumDecoder<T extends Enum<T>> extends TypedDecoder<T> {

	private final T[] values;

	public EnumDecoder(Class<T> type) {
		super(type);
		this.values = type.getEnumConstants();
	}

	public T decode(DataReader reader) {
		int ordinal = reader.readInt();
		return ordinal == -1 ? null : values[ordinal];
	}
}
