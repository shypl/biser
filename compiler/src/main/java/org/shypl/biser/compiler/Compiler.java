package org.shypl.biser.compiler;

import org.shypl.biser.compiler.builder.ModuleBuilderManager;
import org.shypl.biser.compiler.model.ApiSide;
import org.shypl.biser.compiler.model.Model;
import org.shypl.biser.compiler.parser.Parser;
import org.shypl.biser.compiler.parser.ParserException;
import org.shypl.biser.compiler.parser.TokenStream;
import org.shypl.common.util.YamlConfigLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Compiler {

	public static void run(Path configFile) throws CompilerException {
		configFile = configFile.toAbsolutePath();
		final CompilerConfig config;

		try {
			config = YamlConfigLoader.load(configFile, CompilerConfig.class);
		}
		catch (IOException | InstantiationException | IllegalAccessException e) {
			throw new CompilerException("Cannot load configuration file " + configFile, e);
		}

		Path baseDir = configFile.getParent();

		if (config.sourceCode == null) {
			if (config.source == null) {
				config.source = baseDir;
			}
			if (!config.source.isAbsolute()) {
				config.source = baseDir.resolve(config.source);
			}
		}

		prepareModuleConfig(config.server, baseDir);
		prepareModuleConfig(config.client, baseDir);

		run(config);
	}

	public static void run(CompilerConfig config) throws CompilerException {
		Model model = config.sourceCode == null ? parseModel(config.source) : parseModel(config.sourceCode);

		compile(createModule(config.server, ApiSide.SERVER), model);
		compile(createModule(config.client, ApiSide.CLIENT), model);
	}

	public static Model parseModel(String code) throws CompilerException {
		final Parser parser = new Parser();
		try {
			parser.parse(TokenStream.read(new BufferedReader(new StringReader(code))));
		}
		catch (ParserException e) {
			throw new CompilerException("Error on parse model source ", e);
		}

		try {
			return parser.buildModel();
		}
		catch (ParserException e) {
			throw new CompilerException("Error on build model", e);
		}
	}

	public static Model parseModel(Path source) throws CompilerException {
		if (source == null || !Files.exists(source)) {
			throw new CompilerException("Invalid source path (" + source + ")");
		}

		final Parser parser = new Parser();

		if (source.toFile().isFile()) {
			try {
				parser.parse(TokenStream.read(Files.newBufferedReader(source, StandardCharsets.UTF_8)));
			}
			catch (ParserException | IOException e) {
				throw new CompilerException("Error on parse model file " + source, e);
			}
		}
		else {
			for (File file : source.toFile().listFiles((dir, name) -> name.endsWith(".bdm"))) {
				try {
					parser.parse(TokenStream.read(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)));
				}
				catch (ParserException | IOException e) {
					throw new CompilerException("Error on parse model file " + file, e);
				}
			}
		}

		try {
			return parser.buildModel();
		}
		catch (ParserException e) {
			throw new CompilerException("Error on build model", e);
		}
	}

	private static Module createModule(CompilerConfig.ModuleConfig config, ApiSide side) {
		return new Module(config.lang, config.pack, config.target, side);
	}

	private static void prepareModuleConfig(CompilerConfig.ModuleConfig config, Path baseDir) {
		if (!config.target.isAbsolute()) {
			config.target = baseDir.resolve(config.target);
		}
	}

	private static void compile(Module module, Model model) throws CompilerException {
		clearDirectory(module.getTarget().resolve(module.getPackage().replace('.', File.separatorChar)).toFile());
		ModuleBuilderManager.getBuilder(module.getLanguage()).build(module, model);
	}

	private static void clearDirectory(final File directory) throws CompilerException {
		if (directory.exists() && directory.isDirectory()) {
			final File[] files = directory.listFiles();
			if (files != null) {
				for (final File file : files) {
					deleteFile(file);
				}
			}
		}
	}

	private static void deleteFile(final File file) throws CompilerException {
		if (file.getName().startsWith(".")) {
			return;
		}
		if (file.isDirectory()) {
			clearDirectory(file);
		}
		else if (!file.delete()) {
			throw new CompilerException("Can not delete file " + file);
		}
	}

	private Compiler() {
	}
}
