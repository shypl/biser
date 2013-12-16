package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.NamedObject;

public class Property extends NamedObject
{
	public final DataType dataType;

	public Property(String name, DataType dataType)
	{
		super(name);
		this.dataType = dataType;
	}
}
