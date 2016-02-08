package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.Collection;

public class Model {
	private final Collection<StructureType> structures;
	private final Collection<CsiGate>       csiGates;

	public Model(Collection<StructureType> structures, Collection<CsiGate> csiGates) {
		this.csiGates = csiGates;
		this.structures = new ArrayList<>(structures);
	}

	public Collection<StructureType> getStructures() {
		return new ArrayList<>(structures);
	}

	public Collection<CsiGate> getCsiGates() {
		return new ArrayList<>(csiGates);
	}
}
