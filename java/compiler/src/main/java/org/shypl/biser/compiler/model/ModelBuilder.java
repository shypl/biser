package org.shypl.biser.compiler.model;

import java.util.HashMap;
import java.util.Map;

public class ModelBuilder {
	private final Map<String, StructureType> structures = new HashMap<>();
	private final Map<String, CsiGate>       csiGates   = new HashMap<>();

	public EntityType getEntity(String name) throws ModelException {
		StructureType type = structures.get(name);

		if (type == null) {
			type = new EntityType(name);
			structures.put(name, type);
			return (EntityType)type;
		}

		if (type instanceof EntityType) {
			return (EntityType)type;
		}

		throw new ModelException("Structure named " + name + " is not an Entity");
	}

	public EnumType getEnum(String name) throws ModelException {
		StructureType type = structures.get(name);

		if (type == null) {
			type = new EnumType(name);
			structures.put(name, type);
			return (EnumType)type;
		}

		if (type instanceof EnumType) {
			return (EnumType)type;
		}

		throw new ModelException("Structure named " + name + " is not an Enum");
	}

	public StructureType getStructure(String name) {
		return structures.get(name);
	}

	public CsiGate getCsiGate(String name) {
		CsiGate gate = csiGates.get(name);
		if (gate == null) {
			gate = new CsiGate(name);
			csiGates.put(name, gate);
		}
		return gate;
	}

	public Model buildModel() {
		return new Model(structures.values(), csiGates.values());
	}
}
