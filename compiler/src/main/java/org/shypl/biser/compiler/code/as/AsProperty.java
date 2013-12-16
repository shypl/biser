package org.shypl.biser.compiler.code.as;

import org.shypl.biser.compiler.code.CodeProperty;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Mod;

public class AsProperty extends CodeProperty
{
	public AsProperty(String name, String type, Mod mod)
	{
		super(name, type, mod);
	}

	@Override
	public void build(int tab, FileBuilder builder)
	{
		builder.add(tab);

		if (mod.is(Mod.PUBLIC)) {
			builder.add("public ");
		}
		else if (mod.is(Mod.PROTECTED)) {
			builder.add("protected ");
		}
		else if (mod.is(Mod.PRIVATE)) {
			builder.add("private ");
		}

		builder.line("var ", name, ":", type, ";");
	}
}
