package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.NamedObject;
import org.shypl.biser.compiler.Utils;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Class extends NamedObject
{
	public final Package pkg;
	public final Set<DataClass> innerClasses = new LinkedHashSet<>();

	public Class(Package pkg, String name)
	{
		super(name);
		this.pkg = pkg;

		pkg.addClass(this);
	}

	public String className()
	{
		return Utils.toCamelCase(name, true);
	}

	public String scopeName(String s)
	{
		return className();
	}

	public DataClass findInnerClass(String name)
	{
		int i = name.indexOf('.');
		if (i == -1) {
			for (DataClass cls : innerClasses) {
				if (cls.name.equals(name)) {
					return cls;
				}
			}

			return null;
		}

		DataClass cls = findInnerClass(name.substring(0, i));
		return cls == null ? null : cls.findInnerClass(name.substring(i + 1));
	}

	void addInnerClass(DataClass cls)
	{
		innerClasses.add(cls);
	}
}
