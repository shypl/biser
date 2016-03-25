package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.Collection;

public class Model {
	private final Collection<StructureType> structures;
	private final Collection<Api>           apis;

	public Model(Collection<StructureType> structures, Collection<Api> apis) {
		this.apis = apis;
		this.structures = new ArrayList<>(structures);
	}

	public Collection<StructureType> getStructures() {
		return new ArrayList<>(structures);
	}

	public Collection<Api> getApis() {
		return new ArrayList<>(apis);
	}
}
