package org.shypl.biser.compiler.code.java;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.CodeProperty;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Mod;

import java.util.LinkedHashSet;
import java.util.Set;

public class JavaClass extends CodeClass
{
	private final Set<CodeClass> innerClasses  = new LinkedHashSet<>();

	public JavaClass(String name, String parent, Mod mod)
	{
		super(name, parent, mod);
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

		if (mod.is(Mod.STATIC)) {
			builder.add("static ");
		}
		if (mod.is(Mod.ABSTRACT)) {
			builder.add("abstract ");
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

		for (CodeClass innerClass : innerClasses) {
			innerClass.build(tab + 1, builder);
			builder.line();
		}

		builder.line(tab, "}");
	}

	public void addInnerClass(CodeClass cls)
	{
		innerClasses.add(cls);
	}
}
