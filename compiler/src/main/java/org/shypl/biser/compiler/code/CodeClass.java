package org.shypl.biser.compiler.code;

import org.shypl.biser.compiler.prototype.DataType;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CodeClass extends CodeObject
{
	protected final String parent;
	public final    Mod    mod;
	protected final Set<CodeProperty>     properties = new LinkedHashSet<>();
	protected final List<CodeMethod>      methods    = new LinkedList<>();
	private final   Map<DataType, String> encoders   = new HashMap<>();
	private final   Map<DataType, String> decoders   = new HashMap<>();

	public CodeClass(String name, String parent, Mod mod)
	{
		super(name);
		this.parent = parent;
		this.mod = mod;
	}

	public void addProperty(CodeProperty property)
	{
		properties.add(property);
	}

	public void addMethod(CodeMethod method)
	{
		methods.add(method);
	}

	public String addEncoder(DataType type)
	{
		final String name = "_encode" + encoders.size();
		encoders.put(type, name);
		return name;
	}

	public String getEncoder(DataType type)
	{
		return encoders.get(type);
	}

	public String addDecoder(DataType type)
	{
		final String name = "_decode" + decoders.size();
		decoders.put(type, name);
		return name;
	}

	public String getDecoder(DataType type)
	{
		return decoders.get(type);
	}
}
