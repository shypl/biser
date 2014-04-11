package org.shypl.biser.compiler.prototype;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Entity extends EntityContainer
{
	private final String parentName;
	private final Map<String, Parameter> properties   = new LinkedHashMap<>();
	private final Set<String>            enumValues   = new HashSet<>();
	private final Set<String>            encodeStages = new HashSet<>();
	private final Set<String>            decodeStages = new HashSet<>();
	private final boolean isEnum;
	private       Entity  parent;
	private       boolean ignoreFindEntity;
	private       int     id;
	private Set<Entity> children = new LinkedHashSet<>();

	Entity(final EntityContainer container, final String name, final boolean isEnum) throws DuplicateNameException
	{
		this(container, name, null, isEnum);
	}

	Entity(final EntityContainer container, final String name) throws DuplicateNameException
	{
		this(container, name, null, false);
	}

	Entity(final EntityContainer container, final String name, final String parentName) throws DuplicateNameException
	{
		this(container, name, parentName, false);
	}

	private Entity(final EntityContainer container, final String name, final String parentName, final boolean isEnum)
		throws DuplicateNameException
	{
		super(container, name);
		this.parentName = parentName;
		this.isEnum = isEnum;
		container.addEntity(this);
		children.add(this);
	}

	public Entity[] getChildren()
	{
		return children.toArray(new Entity[children.size()]);
	}

	public String[] getEnumValues()
	{
		return enumValues.toArray(new String[enumValues.size()]);
	}

	public int getId()
	{
		return id;
	}

	public Entity getParent()
	{
		return parent;
	}

	public Parameter[] getProperties()
	{
		return properties.values().toArray(new Parameter[properties.size()]);
	}

	public boolean hasParent()
	{
		return parentName != null;
	}

	public boolean hasProperties()
	{
		return !properties.isEmpty();
	}

	public boolean isEnum()
	{
		return isEnum;
	}

	public boolean isOwnedService()
	{
		return owner instanceof Service || (owner instanceof Entity && ((Entity)owner).isOwnedService());
	}

	void addEncodeStage(final String stage)
	{
		encodeStages.add(stage);
	}

	void addDecodeStage(final String stage)
	{
		decodeStages.add(stage);
	}

	public boolean hasEncodeStage(final String stage)
	{
		return encodeStages.contains(stage);
	}

	public boolean hasDecodeStage(final String stage)
	{
		return decodeStages.contains(stage);
	}

	void addEnumValue(final String value) throws DuplicateNameException
	{
		if (enumValues.contains(value)) {
			throw new DuplicateNameException("Duplicate enum value", value);
		}
		enumValues.add(value);
	}

	void addProperty(final Parameter property) throws DuplicateNameException
	{
		if (properties.containsKey(property.name)) {
			throw new DuplicateNameException("Duplicate entity property", property.name);
		}
		properties.put(property.name, property);
	}

	@Override
	Entity findEntity(final String name)
	{
		return ignoreFindEntity ? null : super.findEntity(name);
	}

	@Override
	void linking() throws PrototypeException
	{
		if (hasParent() && parent == null) {
			ignoreFindEntity = true;
			parent = owner.findEntity(parentName);
			ignoreFindEntity = false;
			if (parent == null) {
				throw new PrototypeException("Parent not found (entity: " + name + ", parent: " + parentName + ")");
			}

			// check recursion
			Entity p = parent;
			while (p != null && p.parent != null) {
				p = p.parent;
				if (p == this) {
					throw new PrototypeException(
						"Parents recursion (entity: " + name + ", parent: " + parent.name + ")");
				}
			}
		}

		for (Parameter parameter : properties.values()) {
			parameter.type.linking();
		}

		if (hasParent()) {
			Entity root = parent;
			while (root.hasParent()) {
				root = root.parent;
			}
			id = root.children.size();
			root = parent;
			while (root != null) {
				root.children.add(this);
				root = root.parent;
			}
		}
	}
}
