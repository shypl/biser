package org.shypl.biser.compiler.builder;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CodeMethod extends ModName
{
	public final String result;
	public final  Code                body       = new Code();
	public final  Set<String>         throwsList = new LinkedHashSet<>();
	private final List<CodeParameter> arguments  = new LinkedList<>();

	public CodeMethod(final String name, final int mod, final String result)
	{
		super(name, mod);
		this.result = result;
	}

	public CodeParameter addArgument(final String name, final String type)
	{
		return addArgument(name, type, false);
	}

	public CodeParameter addArgument(final String name, final String type, final boolean isFinal)
	{
		final CodeParameter argument = new CodeParameter(name, type, isFinal ? FINAL : 0);
		arguments.add(argument);
		return argument;
	}

	public CodeParameter[] getArguments()
	{
		return arguments.toArray(new CodeParameter[arguments.size()]);
	}

	public boolean hasArguments()
	{
		return !arguments.isEmpty();
	}
}
