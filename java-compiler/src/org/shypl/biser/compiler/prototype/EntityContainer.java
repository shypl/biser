package org.shypl.biser.compiler.prototype;

import java.util.HashMap;
import java.util.Map;

public class EntityContainer extends Name
{
	protected final EntityContainer owner;
	private final Map<String, Entity> entities = new HashMap<>();
	private boolean ignoreFindEntityInOwner;

	EntityContainer(final EntityContainer owner, final String name)
	{
		super(name);
		this.owner = owner;
	}

	public Entity[] getEntities()
	{
		return entities.values().toArray(new Entity[entities.size()]);
	}

	public boolean hasEntities()
	{
		return !entities.isEmpty();
	}

	public String getFullName()
	{
		return hasOwner() ? owner.getFullName() + '_' + name : name;
	}

	public EntityContainer getOwner()
	{
		return owner;
	}

	public boolean hasOwner()
	{
		return !(owner instanceof Collector);
	}

	void addEntity(final Entity entity) throws DuplicateNameException
	{
		if (entities.containsKey(entity.name)) {
			throw new DuplicateNameException("Duplicate entity", entity.name);
		}
		entities.put(entity.name, entity);
	}

	Entity findEntity(final String name)
	{
		Entity entity = findEntityInside(name);
		if (entity == null && owner != null && !ignoreFindEntityInOwner) {
			ignoreFindEntityInOwner = true;
			entity = owner.findEntity(name);
			ignoreFindEntityInOwner = false;
		}
		return entity;
	}

	void linking() throws PrototypeException
	{
		for (Entity entity : entities.values()) {
			entity.linking();
		}
	}

	private Entity findEntityInside(final String name)
	{
		if (name.indexOf('.') == -1) {
			return entities.get(name);
		}

		final String[] names = name.split("\\.");
		EntityContainer container = this;
		Entity entity = null;

		for (final String name1 : names) {
			entity = container.findEntity(name1);
			if (entity == null) {
				break;
			}
			container = entity;
		}

		return entity;
	}
}
