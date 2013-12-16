package org.shypl.biser.compiler.code.java;

import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Lines;
import org.shypl.biser.compiler.code.Mod;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class JavaMethod extends CodeMethod
{
	private final Set<String> throwsList = new LinkedHashSet<>();

	protected JavaMethod(String name, String result, Mod mod)
	{
		this(name, result, mod, null);
	}

	protected JavaMethod(String name, String result, Mod mod, Lines meta)
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

		if (mod.is(Mod.OVERRIDE)) {
			builder.line(tab, "@Override");
		}

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

		if (mod.is(Mod.STATIC)) {
			builder.add("static ");
		}

		if (mod.is(Mod.ABSTRACT)) {
			builder.add("abstract ");
		}

		if (result != null) {
			builder.add(result, " ");
		}

		builder.add(name, "(");

		int i = arguments.size();
		for (Map.Entry<String, String> entry : arguments.entrySet()) {
			builder.add("final ", entry.getValue(), " ", entry.getKey());
			if (--i != 0) {
				builder.add(", ");
			}
		}
		builder.add(")");

		if (!throwsList.isEmpty()) {
			builder.add(" throws");
			i = throwsList.size();
			for (String s : throwsList) {
				builder.add(" ", s, --i == 0 ? "" : ",");
			}
		}
		if (mod.is(Mod.ABSTRACT)) {
			builder.line(";");
		}
		else {
			builder.line();
			builder.line(tab, "{");

			for (String line : body.lines) {
				builder.line(tab + 1, line);
			}
			builder.line(tab, "}");
		}
	}

	public void addThrow(String cls)
	{
		throwsList.add(cls);
	}
}
