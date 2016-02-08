package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityType extends StructureType {

	private final Map<String, Parameter> fields   = new LinkedHashMap<>();
	private final List<EntityType>       children = new ArrayList<>();
	private EntityType parent;
	private int id             = 0;
	private int childIdCounter = 0;

	public EntityType(String name) {
		super(name);
	}

	public int getId() {
		return id;
	}

	public EntityType getParent() {
		return parent;
	}

	public void setParent(EntityType parent) {
		Objects.requireNonNull(parent);
		if (hasParent()) {
			throw new IllegalStateException("Entity already have parent");
		}
		this.parent = parent;
		id = this.parent.addChild(this);
	}

	public boolean hasParent() {
		return parent != null;
	}

	public void addField(Parameter field) {
		if (hasField(field.getName())) {
			throw new IllegalArgumentException("Entity " + getName() + " already contains field " + field.getName());
		}
		fields.put(field.getName(), field);
	}

	public boolean hasField(String name) {
		return fields.containsKey(name) || hasParent() && parent.hasField(name);
	}

	public List<Parameter> getFields() {
		return new ArrayList<>(fields.values());
	}

	public boolean hasFields() {
		return !fields.isEmpty();
	}

	public boolean hasAllFields() {
		return hasFields() || hasParent() && parent.hasAllFields();
	}

	public List<Parameter> getAllFields() {
		if (hasParent()) {
			List<Parameter> collection = parent.getAllFields();
			collection.addAll(fields.values());
			return collection;
		}
		return getFields();
	}

	@Override
	public <T> T represent(TypeRepresenter<T> representer) {
		return representer.representEntity(this);
	}

	public List<EntityType> getChildren() {
		return new ArrayList<>(children);
	}

	private int addChild(EntityType child) {
		children.add(child);
		return nextChildId();
	}

	private int nextChildId() {
		return hasParent() ? parent.nextChildId() : ++childIdCounter;
	}
}
