package org.shypl.biser.compiler.code.flex;

import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Lines;
import org.shypl.biser.compiler.code.Mod;

import java.util.Map;

public class FlexMethod extends CodeMethod
{
	protected FlexMethod(String name, String result, Mod mod)
	{
		this(name, result, mod, null);
	}

	protected FlexMethod(String name, String result, Mod mod, Lines meta)
	{
		super(name, result, mod, meta);
	}

	@Override
	public void build(int tab, FileBuilder builder)
	{
		if (meta != null) {
			for (String line : meta.lines) {
				builder.line(tab, line);
			}
		}

		if (mod.is(Mod.ABSTRACT)) {
			builder.line(tab, "[Abstract]");
		}

		builder.add(tab);

		if (mod.is(Mod.OVERRIDE)) {
			builder.add("override ");
		}

		if (mod.not(Mod.INTERFACE)) {
			if (mod.is(Mod.PUBLIC)) {
				builder.add("public ");
			}
			else if (mod.is(Mod.PROTECTED)) {
				builder.add("protected ");
			}
			else if (mod.is(Mod.PRIVATE)) {
				builder.add("private ");
			}
		}

		if (mod.is(Mod.STATIC)) {
			builder.add("static ");
		}

		if (mod.is(Mod.FINAL)) {
			builder.add("final ");
		}

		builder.add("function ");

		if (mod.is(Mod.GETTER)) {
			builder.add("get ");
		}

		builder.add(name, "(");

		int i = arguments.size();
		for (Map.Entry<String, String> entry : arguments.entrySet()) {
			builder.add(entry.getKey(), ":", entry.getValue());
			if (--i != 0) {
				builder.add(", ");
			}
		}
		builder.add(")");

		if (result != null) {
			builder.add(":", result);
		}

		if (mod.is(Mod.INTERFACE)) {
			builder.line(";");
		}
		else {

			builder.line();
			builder.line(tab, "{");

			if (mod.is(Mod.ABSTRACT)) {
				builder.line(tab + 1, "throw new AbstractMethodException();");
			}
			else {
				for (String line : body.lines) {
					builder.line(tab + 1, line);
				}
			}
			builder.line(tab, "}");
		}
	}
}
