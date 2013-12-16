package org.shypl.biser.compiler.prototype;

import java.util.LinkedHashSet;
import java.util.Set;

public class EnumDataClass extends DataClass
{
	public final Set<String> values = new LinkedHashSet<>();

	public EnumDataClass(Package pkg, String name, Class scope)
	{
		super(pkg, name, scope);
	}

	public void addValue(String value)
	{
		values.add(value);
	}
}
