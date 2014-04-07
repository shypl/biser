package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.Utils;

public abstract class Type
{
	@Override
	public boolean equals(final Object obj)
	{
		return this == obj || (obj instanceof Type && ((Type)obj).name().equals(name()));
	}

	@Override
	public int hashCode()
	{
		return name().hashCode();
	}

	public abstract String name();

	abstract void linking() throws PrototypeException;

	public final static class Primitive extends Type
	{
		public static final  Primitive   BOOL   = new Primitive("bool");
		public static final  Primitive   BYTE   = new Primitive("byte");
		public static final  Primitive   BYTES  = new Primitive("bytes");
		public static final  Primitive   INT    = new Primitive("int");
		public static final  Primitive   UINT   = new Primitive("uint");
		public static final  Primitive   DOUBLE = new Primitive("double");
		public static final  Primitive   STRING = new Primitive("string");
		private static final Primitive[] list   = new Primitive[]{
			BOOL, BYTE, BYTES, INT, UINT, DOUBLE, STRING
		};

		public static Primitive define(final String name)
		{
			for (Primitive primitive : list) {
				if (primitive.name.equals(name)) {
					return primitive;
				}
			}

			return null;
		}

		final String name;

		private Primitive(final String name)
		{
			this.name = name;
		}

		@Override
		public String name()
		{
			return name;
		}

		@Override
		void linking()
		{}
	}

	public final static class Entity extends Type
	{
		private String                                    entityName;
		private EntityContainer                           container;
		private org.shypl.biser.compiler.prototype.Entity entity;

		Entity(final String entityName, final EntityContainer container)
		{
			this.entityName = entityName;
			this.container = container;
		}

		Entity(org.shypl.biser.compiler.prototype.Entity entity)
		{
			this.entity = entity;
		}

		public org.shypl.biser.compiler.prototype.Entity entity()
		{
			return entity;
		}

		@Override
		public String name()
		{
			return entity.getFullName();
		}

		@Override
		void linking() throws PrototypeException
		{
			if (entityName != null && entity == null) {
				entity = container.findEntity(entityName);
				if (entity == null) {
					throw new PrototypeException("Entity for not found (" + entityName + ")");
				}
			}
		}
	}

	public final static class List extends Type
	{
		public final Type    type;
		public final boolean isArray;

		List(final Type type, final boolean isArray)
		{
			this.type = type;
			this.isArray = isArray;
		}

		@Override
		public String name()
		{
			return Utils.toCamelCase(type.name()) + (isArray ? "Array" : "List");
		}

		@Override
		void linking() throws PrototypeException
		{
			type.linking();
		}
	}

	public final static class Map extends Type
	{
		public final Type key;
		public final Type value;

		Map(final Type key, final Type value)
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public String name()
		{
			return Utils.toCamelCase(key.name()) + "To" + Utils.toCamelCase(value.name()) + "Map";
		}

		@Override
		void linking() throws PrototypeException
		{
			key.linking();
			value.linking();
		}
	}
}
