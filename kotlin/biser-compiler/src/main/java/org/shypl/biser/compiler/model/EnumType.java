package org.shypl.biser.compiler.model;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class EnumType extends StructureType {

	private final Set<String> values = new LinkedHashSet<>();

	public EnumType(String name) {
		super(name);
	}

	@Override
	public <T> T represent(TypeRepresenter<T> representer) {
		return representer.representEnum(this);
	}

	public void addValue(String value) {
		if (!values.add(value)) {
			throw new IllegalArgumentException("Enum " + getName() + " already contains value " + value);
		}
	}

	public Set<String> getValues() {
		return new LinkedHashSet<>(values);
	}
	
	public boolean isEnum() {
		return true;
	}
	
	@NotNull
	@Override
	public EnumType asEnumType() {
		return this;
	}
}
