package org.shypl.biser.compiler.code;

public abstract class CodeNamedObject {
	private final String name;

	public CodeNamedObject(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
