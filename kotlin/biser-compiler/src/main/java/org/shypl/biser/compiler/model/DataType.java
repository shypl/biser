package org.shypl.biser.compiler.model;

import org.jetbrains.annotations.NotNull;

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
	
	public boolean isEntity() {
		return false;
	}
	
	public boolean isEnum() {
		return false;
	}
	
	@NotNull
	public EnumType asEnumType() {
		throw new UnsupportedOperationException();
	}
	
	protected void setName(String name) {
		this.name = name;
	}

	public abstract <T> T represent(TypeRepresenter<T> representer);
	
	public int getId() {
		throw new UnsupportedOperationException();
	}
}
