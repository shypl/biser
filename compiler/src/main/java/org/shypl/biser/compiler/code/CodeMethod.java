package org.shypl.biser.compiler.code;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CodeMethod extends CodeObject
{
	public final Lines               body      = new Lines();
	public final Map<String, String> arguments = new LinkedHashMap<>();
	protected final String result;
	protected final Mod    mod;
	protected final Lines  meta;

	protected CodeMethod(String name, String result, Mod mod, Lines meta)
	{
		super(name);
		this.result = result;
		this.mod = mod;
		this.meta = meta;
	}

	public void addArgument(String name, String type)
	{
		arguments.put(name, type);
	}
}
