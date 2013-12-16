package org.shypl.biser.compiler.code.java;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Mod;

import java.util.Set;

public class JavaEnumClass extends CodeClass
{
	private final Set<String> values;

	public JavaEnumClass(String name, Set<String> values)
	{
		super(name, null, Mod.at(0));
		this.values = values;
	}

	@Override
	public void build(int tab, FileBuilder builder)
	{
		if (tab > 0) {
			builder.line(tab, "public enum ", name);
		}
		else {
			builder.line(tab, "public static enum ", name);
		}
		builder.line(tab, "{");

		int i = values.size();
		for (String value : values) {
			builder.line(tab + 1, value, --i == 0 ? ";" : ",");
		}

		builder.line();
		builder.line(tab + 1, "public static ", name, " valueOf(int ordinal)");
		builder.line(tab + 1, "{");
		builder.line(tab + 2, "switch (ordinal) {");
		i = 0;
		for (String value : values) {
			builder.line(tab + 3, "case ", String.valueOf(i++), ":");
			builder.line(tab + 4, "return ", value, ";");
		}
		builder.line(tab + 3, "case -1:");
		builder.line(tab + 4, "return null;");
		builder.line(tab + 3, "default:");
		builder.line(tab + 4, "throw new IllegalArgumentException(\"No enum constant \" + " + name + ".class.getCanonicalName() + \" at \" + ordinal);");
		builder.line(tab + 2, "}");
		builder.line(tab + 1, "}");
		/*
		public static En valueOf(int ordinal)
		{
			switch (ordinal) {
				default:
					return null;
			}
		}
		 */

		builder.line(tab, "}");
	}
}
