package org.shypl.biser.compiler.code.as;

import org.shypl.biser.compiler.code.CodeFile;
import org.shypl.biser.compiler.code.FileBuilder;

public class AsFile extends CodeFile
{
	protected AsFile(String pkg)
	{
		super("as", pkg);
	}

	@Override
	protected void build(FileBuilder builder)
	{
		builder.line("package ", pkg, "");
		builder.line("{");

		for (String i : imports) {
			builder.line(1, "import ", i, ";");
		}

		builder.line("");

		cls.build(1, builder);

		builder.line("}");
	}
}
