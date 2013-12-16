package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.NamedObject;

import java.util.LinkedHashSet;
import java.util.Set;

public class Package extends NamedObject
{
	private final Package parent;
	private final Set<Package> children     = new LinkedHashSet<>();
	private final Set<Class> classes = new LinkedHashSet<>();

	protected Package(String name, Package parent)
	{
		super(name);

		if (name != null) {
			if (name.lastIndexOf('.') != -1) {
				throw new IllegalArgumentException();
			}
		}

		this.parent = parent;

		if (parent != null) {
			this.parent.children.add(this);
		}
	}

	public Package child(String name)
	{
		int i = name.indexOf('.');

		if (i != -1) {
			return child(name.substring(0, i)).child(name.substring(i + 1));
		}

		for (Package child : children) {
			if (child.name.equals(name)) {
				return child;
			}
		}

		return new Package(name, this);
	}

	public Package root()
	{
		return parent == null ? this : parent.root();
	}

	public String fullName()
	{
		return parent == null ? name : (parent.fullName() + "." + name);
	}

	public Class findClass(String name)
	{
		int i = name.indexOf('.');
		if (i == -1) {
			for (Class cls : classes) {
				if (cls.name.equals(name)) {
					return cls;
				}
			}
			return null;
		}

		String subName = name.substring(0, i);

		for (Package child : children) {
			if (child.name.equals(subName)) {
				Class cls = child.findClass(name.substring(i + 1));
				if (cls != null) {
					return cls;
				}
			}
		}

		Class cls = findClass(subName);
		return cls == null ? null : cls.findInnerClass(name.substring(i + 1));
	}

	void addClass(Class cls)
	{
		classes.add(cls);
	}
}
