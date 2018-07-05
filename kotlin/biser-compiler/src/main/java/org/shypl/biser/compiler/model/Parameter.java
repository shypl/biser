package org.shypl.biser.compiler.model;

public class Parameter {
	private final String   name;
	private final DataType type;

	public Parameter(String name, DataType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return name + ":" + type.getName();
	}

	public String getName() {
		return name;
	}

	public DataType getType() {
		return type;
	}
}
