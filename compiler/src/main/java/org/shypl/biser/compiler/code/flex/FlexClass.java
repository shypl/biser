package org.shypl.biser.compiler.code.flex;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.CodeProperty;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Mod;

public class FlexClass extends CodeClass
{
	public FlexClass(String name, String parent, Mod mod)
	{
		super(name, parent, mod);
	}

	@Override
	public void build(int tab, FileBuilder builder)
	{
		if (mod.is(Mod.ABSTRACT)) {
			builder.line(tab, "[Abstract]");
		}

		builder.add(tab);

		if (mod.is(Mod.PUBLIC)) {
			builder.add("public ");
		}

		builder.add(mod.is(Mod.INTERFACE) ? "interface " : "class ", name);

		if (parent == null) {
			builder.line();
		}
		else {
			builder.line(" extends ", parent);
		}

		builder.line(tab, "{");

		for (CodeProperty property : properties) {
			property.build(tab + 1, builder);
		}

		if (!properties.isEmpty()) {
			builder.line();
		}

		for (CodeMethod method : methods) {
			method.build(tab + 1, builder);
			builder.line();
		}

		builder.line(tab, "}");
	}
}
