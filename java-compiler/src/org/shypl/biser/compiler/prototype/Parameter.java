package org.shypl.biser.compiler.prototype;

public class Parameter extends Name
{
	public final Type type;

	Parameter(final String name, final Type type)
	{
		super(name);
		this.type = type;
	}
}
