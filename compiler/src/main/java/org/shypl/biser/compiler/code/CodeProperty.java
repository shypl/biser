package org.shypl.biser.compiler.code;

public abstract class CodeProperty extends CodeObject
{
	protected final String type;
	protected final Mod mod;

	public CodeProperty(String name, String type, Mod mod)
	{
		super(name);
		this.type = type;
		this.mod = mod;
	}
}
