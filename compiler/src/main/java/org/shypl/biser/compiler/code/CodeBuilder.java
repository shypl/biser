package org.shypl.biser.compiler.code;

import org.shypl.biser.compiler.prototype.ApiClass;
import org.shypl.biser.compiler.prototype.Class;
import org.shypl.biser.compiler.prototype.Package;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public abstract class CodeBuilder
{
	protected final File path;

	public CodeBuilder(Path path)
	{
		this.path = path.toFile();
	}

	public abstract void build(Class cls) throws IOException;

	public abstract void buildApiController(Package pkg, Set<ApiClass> classes) throws IOException;
}