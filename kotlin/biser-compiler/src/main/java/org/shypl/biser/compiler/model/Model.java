package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.Collection;

public class Model {
	private final Collection<StructureType> structures;
	private final Api                       api;

	public Model(Collection<StructureType> structures, Api api) {
		this.structures = new ArrayList<>(structures);
		this.api = api;
	}

	public Collection<StructureType> getStructures() {
		return new ArrayList<>(structures);
	}

	public Api getApi() {
		return api;
	}
}
