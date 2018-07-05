package org.shypl.biser.compiler.parser;

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
}
