package org.shypl.biser.compiler.model;

import java.util.HashMap;
import java.util.Map;

public final class PrimitiveType extends DataType {

	public static final PrimitiveType BYTE;
	public static final PrimitiveType BOOL;
	public static final PrimitiveType INT;
	public static final PrimitiveType UINT;
	public static final PrimitiveType LONG;
	public static final PrimitiveType ULONG;
	public static final PrimitiveType DOUBLE;
	public static final PrimitiveType STRING;
	public static final PrimitiveType DATE;
	public static final PrimitiveType BYTES;

	private static final Map<String, PrimitiveType> map = new HashMap<>(10);

	public static PrimitiveType get(String name) {
		return map.get(name);
	}

	static {
		BYTE = new PrimitiveType("byte");
		BOOL = new PrimitiveType("bool");
		INT = new PrimitiveType("int");
		UINT = new PrimitiveType("uint");
		LONG = new PrimitiveType("long");
		ULONG = new PrimitiveType("ulong");
		DOUBLE = new PrimitiveType("double");
		STRING = new PrimitiveType("string");
		DATE = new PrimitiveType("date");
		BYTES = new PrimitiveType("bytes");
	}

	private PrimitiveType(String name) {
		super(name);
		map.put(name, this);
	}

	@Override
	public <T> T represent(TypeRepresenter<T> representer) {
		return representer.representPrimitive(this);
	}
}
