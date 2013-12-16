package org.shypl.biser.compiler;

public abstract class NamedObject
{
	public final String name;

	protected NamedObject(String name)
	{
		this.name = name;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj || (obj instanceof NamedObject && ((NamedObject)obj).name.equals(name));
	}
}
