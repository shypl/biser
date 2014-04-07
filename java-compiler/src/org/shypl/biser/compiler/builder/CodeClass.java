package org.shypl.biser.compiler.builder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CodeClass extends ModName
{
	public final String pkg;
	public final  List<String>        generics     = new LinkedList<>();
	private final List<CodeParameter> properties   = new LinkedList<>();
	private final List<CodeMethod>    constructors = new LinkedList<>();
	private final List<CodeMethod>    methods      = new LinkedList<>();
	private final List<CodeClass>     inners       = new LinkedList<>();
	private final Set<String>         imports      = new TreeSet<>();
	private String[]  enumValues;
	private CodeClass owner;
	private String    parent;

	public CodeClass(final String pkg, final String name, final int mod)
	{
		super(name, mod);
		this.pkg = pkg;
	}

	public CodeMethod addConstructor()
	{
		final CodeMethod method = new CodeMethod(name, PUBLIC, null);
		constructors.add(method);
		return method;
	}

	public void addImport(final String value)
	{
		final String s = value.substring(0, value.lastIndexOf('.'));
		if (!pkg.equals(s)) {
			imports.add(value);
		}
	}

	public CodeClass addImport(final CodeClass target)
	{
		if (!target.pkg.equals(pkg)) {
			CodeClass t = target;
			while (t.isInner()) {
				t = t.owner;
			}
			imports.add(t.pkg + '.' + t.name);
		}
		return target;
	}

	public CodeClass addInner(final CodeClass inner)
	{
		inners.add(inner);
		inner.owner = this;
		inner.mod |= STATIC;
		return inner;
	}

	public CodeMethod addMethod(final String name, final int mod, final String result)
	{
		final CodeMethod method = new CodeMethod(name, mod, result);
		methods.add(method);
		return method;
	}

	public CodeParameter addProperty(final String name, final String type, final int mod)
	{
		final CodeParameter property = new CodeParameter(name, type, mod);
		properties.add(property);
		return property;
	}

	public void declareAsEnum(final String[] enumValues)
	{
		this.enumValues = enumValues.clone();
	}

	public CodeMethod getConstructor()
	{
		return constructors.get(0);
	}

	public CodeMethod[] getConstructors()
	{
		return constructors.toArray(new CodeMethod[constructors.size()]);
	}

	public String[] getEnumValues()
	{
		return enumValues;
	}

	public String[] getImports()
	{
		for (CodeClass inner : inners) {
			for (String value : inner.getImports()) {
				imports.add(value);
			}
		}

		return imports.toArray(new String[imports.size()]);
	}

	public CodeClass[] getInners()
	{
		return inners.toArray(new CodeClass[inners.size()]);
	}

	public CodeMethod[] getMethods()
	{
		return methods.toArray(new CodeMethod[methods.size()]);
	}

	public String getNameFor(CodeClass target)
	{
		if (owner == null) {
			return name;
		}

		String fullName = name;
		CodeClass next = owner;

		while (next != target && next != null) {
			fullName = next.name + '.' + fullName;
			next = next.owner;
		}

		return fullName;
	}

	public String getParent()
	{
		return parent;
	}

	public CodeParameter[] getProperties()
	{
		return properties.toArray(new CodeParameter[properties.size()]);
	}

	public boolean hasConstructors()
	{
		return !constructors.isEmpty();
	}

	public boolean hasParent()
	{
		return parent != null;
	}

	public boolean isEnum()
	{
		return enumValues != null;
	}

	public boolean isInner()
	{
		return owner != null;
	}

	public void setParent(final String parent)
	{
		this.parent = parent;
	}

	public void setParent(final CodeClass parent)
	{
		addImport(parent);
		this.parent = parent.getNameFor(this);
	}
}
