package org.shypl.biser.compiler;

import org.shypl.biser.compiler.code.CodeBuilder;
import org.shypl.biser.compiler.code.flex.FlexBuilder;
import org.shypl.biser.compiler.code.java.JavaBuilder;
import org.shypl.biser.compiler.prototype.ApiClass;
import org.shypl.biser.compiler.prototype.Class;
import org.shypl.biser.compiler.prototype.ObjectDataClass;
import org.shypl.biser.compiler.prototype.RootPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class Compiler
{
	private final Charset charset;
	private final RootPackage                                   pkg        = new RootPackage();
	private final Set<Class> classes    = new LinkedHashSet<>();
	private final Set<ApiClass>                                 apiClasses = new LinkedHashSet<>();

	public Compiler()
	{
		this(Charset.forName("UTF-8"));
	}

	public Compiler(final Charset charset)
	{
		this.charset = charset;
	}

	public void load(Path source) throws IOException, TokenizerException
	{
		System.out.println("Load prototypes from " + source);

		loadFiles(source.toFile(), pkg);

		for (Class cls : classes) {
			if (cls instanceof ObjectDataClass) {
				((ObjectDataClass)cls).prepare();
			}
		}
	}

	public void compile(Lang lang, Path path, String pkg) throws IOException
	{
		System.out.println("Compile " + lang + " to " + path);

		CodeBuilder builder = null;

		switch (lang) {
			case JAVA:
				builder = new JavaBuilder(path);
				break;
			case FLEX:
				builder = new FlexBuilder(path);
				break;
		}

		this.pkg.setTarget(pkg);

		for (Class cls : classes) {
			builder.build(cls);
		}

		if (!apiClasses.isEmpty()) {
			builder.buildApiController(this.pkg, apiClasses);
		}

		this.pkg.setTarget(null);
	}

	private void loadFiles(File dir, org.shypl.biser.compiler.prototype.Package pkg)
		throws IOException, TokenizerException
	{
		File[] files = dir.listFiles();

		if (files == null) {
			return;
		}

		for (File file : files) {
			if (file.isDirectory()) {
				loadFiles(file, pkg.child(file.getName()));
			}
			else if (file.getName().endsWith(".bpt")) {
				loadFile(file, pkg);
			}
		}
	}

	private void loadFile(File file, org.shypl.biser.compiler.prototype.Package pkg) throws IOException, TokenizerException
	{
		System.out.println(" - " + file);

		Tokenizer tokenizer = new Tokenizer(new BufferedReader(new InputStreamReader(new FileInputStream(file), charset)));
		final Parser parser = new Parser(pkg, tokenizer);
		while (tokenizer.hasNext()) {
			Class cls = parser.parseClass();
			classes.add(cls);
			if (cls instanceof ApiClass) {
				apiClasses.add((ApiClass)cls);
			}
		}
	}
}
