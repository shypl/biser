package org.shypl.biser.compiler.code.as;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.FileBuilder;
import org.shypl.biser.compiler.code.Mod;

import java.util.Set;

public class AsEnumClass extends CodeClass
{
	private final Set<String> values;

	public AsEnumClass(String name, Set<String> values)
	{
		super(name, "Enum", Mod.at(0));
		this.values = values;
	}

	@Override
	public void build(int tab, FileBuilder builder)
	{
		builder.line(tab, "public final class ", name, " extends Enum");
		builder.line(tab, "{");

		for (String value : values) {
			builder.line(tab + 1, "public static const ", value, ":", name, " = new ", name, "(\"", value, "\");");
		}
		builder.line();

		builder.line(tab + 1, "public static function valueOf(ordinal:int):", name);
		builder.line(tab + 1, "{");
		builder.line(tab + 2, "switch (ordinal) {");
		for (String value : values) {
			builder.line(tab + 3, "case ", value, ".ordinal:");
			builder.line(tab + 4, "return ", value, ";");
		}
		builder.line(tab + 3, "case -1:");
		builder.line(tab + 4, "return null;");
		builder.line(tab + 3, "default:");
		builder.line(tab + 4, "throw new Error(\"No enum constant " + name + " at \" + ordinal);");
		builder.line(tab + 2, "}");
		builder.line(tab + 1, "}");
		builder.line();

		builder.line(tab + 1, "public function ", name, "(name:String)");
		builder.line(tab + 1, "{");
		builder.line(tab + 2, "super(name);");
		builder.line(tab + 1, "}");

		builder.line(tab, "}");
	}
}
