package org.shypl.biser.compiler.parser;

import org.jetbrains.annotations.NotNull;
import org.shypl.biser.compiler.model.EnumType;
import org.shypl.biser.compiler.model.StructureType;
import org.shypl.biser.compiler.model.TypeRepresenter;

public class StructureTypeProxy extends StructureType {
	public StructureType source;
	
	public StructureTypeProxy(String name) {
		super(name);
	}
	
	@Override
	public <T> T represent(TypeRepresenter<T> representer) {
		if (source == null) {
			throw new UnsupportedOperationException("Source is not set");
		}
		return source.represent(representer);
	}
	
	@Override
	public int getId() {
		return source.getId();
	}
	
	void setSource(StructureType source) {
		this.source = source;
	}
	
	@Override
	public boolean isEntity() {
		return source.isEntity();
	}
	
	@Override
	public boolean isEnum() {
		return source.isEnum();
	}
	
	@NotNull
	public EnumType asEnumType() {
		return source.asEnumType();
	}
}
