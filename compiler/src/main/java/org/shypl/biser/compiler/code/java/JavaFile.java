package org.shypl.biser.compiler.code.java;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeFile;
import org.shypl.biser.compiler.code.FileBuilder;

public class JavaFile extends CodeFile
{
	protected JavaFile(String pkg)
	{
		super("java", pkg);
	}

	@Override
	public void setClass(CodeClass cls)
	{
		super.setClass(cls);

		if (name.indexOf('<') != -1) {
			name = name.substring(0, name.indexOf('<'));
		}
	}

	@Override
	protected void build(FileBuilder builder)
	{
		builder.line("package ", pkg, ";");
		builder.line();

		for (String i : imports) {
			builder.line("import ", i, ";");
		}

		builder.line();
		cls.build(0, builder);
	}
}
