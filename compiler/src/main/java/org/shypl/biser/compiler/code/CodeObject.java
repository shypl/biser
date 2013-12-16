package org.shypl.biser.compiler.code;

import org.shypl.biser.compiler.NamedObject;

public abstract class CodeObject extends NamedObject
{
	protected CodeObject(String name)
	{
		super(name);
	}

	public abstract void build(int tab, FileBuilder builder);
}
