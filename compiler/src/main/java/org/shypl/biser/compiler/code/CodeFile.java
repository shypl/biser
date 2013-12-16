package org.shypl.biser.compiler.code;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class CodeFile
{
	private final   String    ext;
	protected final String    pkg;
	protected       CodeClass cls;
	private Set<String> importsSet = new LinkedHashSet<>();
	protected List<String> imports;
	protected String       name;

	protected CodeFile(String ext, String pkg)
	{
		this.ext = ext;
		this.pkg = pkg;
	}

	public void save(File dir) throws IOException
	{
		System.out.println(" - " + pkg + "." + name);

		imports = new LinkedList<>(importsSet);
		Collections.sort(imports);

		FileBuilder builder = new FileBuilder();
		build(builder);
		builder.save(new File(dir, pkg.replace('.', '/') + "/" + name + "." + ext));
	}

	protected abstract void build(FileBuilder builder);

	public void setClass(CodeClass cls)
	{
		this.name = cls.name;
		this.cls = cls;
	}

	public void addImport(String name)
	{
		importsSet.add(name);
	}
}
