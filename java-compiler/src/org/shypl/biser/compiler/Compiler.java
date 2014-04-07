package org.shypl.biser.compiler;

import org.shypl.biser.compiler.builder.Builder;
import org.shypl.biser.compiler.builder.BuilderFlash;
import org.shypl.biser.compiler.builder.BuilderJava;
import org.shypl.biser.compiler.prototype.Collector;
import org.shypl.biser.compiler.prototype.PrototypeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public class Compiler
{
	public static void compile(File config) throws CompilerException, IOException
	{
		config = config.getAbsoluteFile();
		final Properties props = new Properties();

		try {
			props.load(new FileInputStream(config));
		}
		catch (IOException e) {
			throw new CompilerException("Cannot load configuration file (" + config + ")", e);
		}

		final File root = config.getParentFile();

		//		final Collector collector = new Collector();

		final Compiler compiler = new Compiler();
		compiler.loadDirectory(new File(root, props.getProperty("path", ".")));

		final String[] stages = props.getProperty("stages", "single,server,client").split(",\\s*");

		for (String stage : stages) {
			final String path = props.getProperty(stage + ".path");

			if (path != null) {
				final String pkg = props.getProperty(stage + ".package", "");
				final Side side = Side.valueOf(props.getProperty(stage + ".side", stage).toUpperCase());
				final Lang lang = Lang.valueOf(props.getProperty(stage + ".lang",
					(side == Side.CLIENT ? Lang.FLASH : Lang.JAVA).toString()).toUpperCase());

				compiler.compile(lang, new File(root, path).toPath(), pkg, side);
			}
		}
	}

	private final Collector collector = new Collector();
	private final Charset charset;

	public Compiler()
	{
		this(StandardCharsets.UTF_8);
	}

	public Compiler(final Charset charset)
	{
		this.charset = charset;
	}

	public void compile(final Lang lang, final Path path, final String packageName)
		throws IOException, PrototypeException
	{
		compile(lang, path, packageName, Side.SINGLE);
	}

	public void compile(final Lang lang, final Path path, final String packageName, final Side side)
		throws IOException, PrototypeException
	{
		clearDirectory(path.resolve(packageName.replace('.', '/')).toFile());
		createBuilder(lang, path, packageName, side).build(collector.getEntities(), collector.getServices());
	}

	public void loadDirectory(final File path) throws CompilerException
	{
		for (File file : path.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				return name.endsWith(".bpt");
			}
		})) {
			loadFile(file);
		}
	}

	public void loadFile(File file) throws CompilerException
	{
		try {
			file = file.getCanonicalFile();
			collector.add(new BufferedReader(new InputStreamReader(new FileInputStream(file), charset)));
		}
		catch (PrototypeException e) {
			throw new CompilerException("Cannot parse file " + file + " [" + e.getMessage() + "]", e);
		}
		catch (IOException e) {
			throw new CompilerException("Cannot load file " + file + "", e);
		}
	}

	private void clearDirectory(final File directory) throws IOException
	{
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				throw new IllegalArgumentException(directory + " is not a directory");
			}

			final File[] files = directory.listFiles();
			if (files == null) {
				throw new IOException("Failed to list contents of " + directory);
			}

			for (final File file : files) {
				deleteFile(file);
			}
		}
	}

	private Builder createBuilder(final Lang lang, final Path path, final String packageName, final Side side)
	{
		switch (lang) {
			case JAVA:
				return new BuilderJava(path, packageName, side);
			case FLASH:
				return new BuilderFlash(path, packageName, side);
		}

		throw new IllegalArgumentException();
	}

	private void deleteFile(final File file) throws IOException
	{
		if (file.isDirectory()) {
			clearDirectory(file);
		}
		else {
			if (file.getName().startsWith(".")) {
				return;
			}
			if (!file.delete()) {
				throw new IOException("Unable to delete " + file);
			}
		}
	}
}
