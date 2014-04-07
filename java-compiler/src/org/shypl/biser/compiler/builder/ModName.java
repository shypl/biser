package org.shypl.biser.compiler.builder;

public class ModName extends Mod
{
	public final String name;

	public ModName(final String name, final int mod)
	{
		super(mod);
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
