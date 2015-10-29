package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.Collection;

public class Model {
	private final Collection<StructureType> structures;
	private final Collection<ApiGate>       apiGates;

	public Model(Collection<StructureType> structures, Collection<ApiGate> apiGates) {
		this.apiGates = apiGates;
		this.structures = new ArrayList<>(structures);
	}

	public Collection<StructureType> getStructures() {
		return new ArrayList<>(structures);
	}

	public Collection<ApiGate> getApiGates() {
		return new ArrayList<>(apiGates);
	}
}
