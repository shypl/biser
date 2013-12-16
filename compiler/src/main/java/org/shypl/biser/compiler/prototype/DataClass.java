package org.shypl.biser.compiler.prototype;

public abstract class DataClass extends Class
{
	public final Class scope;

	public DataClass(Package pkg, String name, Class scope)
	{
		super(pkg, name);
		this.scope = scope;
		if (scope != null) {
			scope.addInnerClass(this);
		}
	}

	@Override
	public String scopeName(String s)
	{
		return scope == null ? super.className() : (scope.scopeName(s) + s + super.className());
	}

	public Class scopeRoot()
	{
		return scope == null ? this : (scope instanceof DataClass ? ((DataClass)scope).scopeRoot() : scope);
	}
}
