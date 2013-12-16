package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.NamedObject;

import java.util.LinkedHashSet;
import java.util.Set;

public class Method extends NamedObject
{
	public final boolean isService;
	public final Set<Property> properties = new LinkedHashSet<>();
	public DataType result;

	public Method(String name, boolean isService)
	{
		super(name);
		this.isService = isService;
	}

	public void addProperty(Property property)
	{
		properties.add(property);
	}

	public void serResult(DataType type)
	{
		result = type;
	}

	public boolean hasResult()
	{
		return result != null;
	}
}
