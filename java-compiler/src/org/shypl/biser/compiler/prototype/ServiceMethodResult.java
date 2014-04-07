package org.shypl.biser.compiler.prototype;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ServiceMethodResult
{
	abstract void linking() throws PrototypeException;

	public abstract static class TypeRef extends ServiceMethodResult
	{
		public final Type type;

		TypeRef(final Type type)
		{
			this.type = type;
		}

		@Override
		void linking() throws PrototypeException
		{
			type.linking();
		}
	}

	public final static class Simple extends TypeRef
	{
		Simple(final Type type)
		{
			super(type);
		}
	}

	public final static class Lazy extends TypeRef
	{
		Lazy(final Type type)
		{
			super(type);
		}
	}

	public final static class LazyMulti extends ServiceMethodResult
	{
		private final Map<String, Parameter> parameters = new LinkedHashMap<>();

		public Parameter[] getParameters()
		{
			return parameters.values().toArray(new Parameter[parameters.size()]);
		}

		void addParameter(final Parameter parameter) throws DuplicateNameException
		{
			if (parameters.containsKey(parameter.name)) {
				throw new DuplicateNameException("Duplicate result parameter", parameter.name);
			}
			parameters.put(parameter.name, parameter);
		}

		@Override
		void linking() throws PrototypeException
		{
			for (Parameter parameter : parameters.values()) {
				parameter.type.linking();
			}
		}
	}
}
