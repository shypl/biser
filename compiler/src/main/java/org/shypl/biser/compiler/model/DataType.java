package org.shypl.biser.compiler.model;

public abstract class DataType {
	private String name;

	public DataType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public abstract <T> T represent(TypeRepresenter<T> representer);
}
