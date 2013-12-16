package org.shypl.biser.compiler.prototype;

import java.util.LinkedHashSet;
import java.util.Set;

public class ObjectDataClass extends DataClass
{
	public final Set<Property>                 properties = new LinkedHashSet<>();
	private final String          parentName;
	private       ObjectDataClass parent;

	public ObjectDataClass(Package pkg, String name, Class scope, String parentName)
	{
		super(pkg, name, scope);
		this.parentName = parentName;
	}

	public void prepare()
	{
		if (hasParent()) {
			ObjectDataClass rootParent = getParent();
			while (rootParent.hasParent()) {
				rootParent = rootParent.getParent();
			}
		}
	}

	public boolean hasParent()
	{
		return parentName != null;
	}

	public ObjectDataClass getParent()
	{
		if (parent == null) {
			if (scope != null) {
				parent = (ObjectDataClass)scope.findInnerClass(parentName);
			}
			if (parent == null) {
				parent = (ObjectDataClass)pkg.findClass(parentName);
			}

			if (parent == null) {
				throw new RuntimeException("Parent class not found (" + scopeName(".") + ")");
			}
		}

		return parent;
	}

	public void addProperty(Property property)
	{
		properties.add(property);
	}
}
