package org.shypl.biser.compiler.builder;

public class CodeParameter extends ModName
{
	public final String type;
	public final int    mod;

	public CodeParameter(final String name, final String type, final int mod)
	{
		super(name, mod);
		this.type = type;
		this.mod = mod;
	}
}
