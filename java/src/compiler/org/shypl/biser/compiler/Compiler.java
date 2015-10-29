package org.shypl.biser.compiler;

import org.shypl.biser.compiler.builder.ModuleBuilderManager;
import org.shypl.biser.compiler.model.Model;
import org.shypl.biser.compiler.parser.Parser;
import org.shypl.biser.compiler.parser.ParserException;
import org.shypl.biser.compiler.parser.TokenStream;
import org.shypl.common.util.YamlConfigLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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

		if (config.source == null) {
			config.source = baseDir;
		}
		if (!config.source.isAbsolute()) {
			config.source = baseDir.resolve(config.source);
		}

		for (Map.Entry<String, CompilerConfig.ModuleConfig> entry : config.modules.entrySet()) {
			String moduleName = entry.getKey();
			CompilerConfig.ModuleConfig moduleConfig = entry.getValue();

			if (moduleConfig.lang == null) {
				throw new CompilerException("For module " + moduleName + " not specified lang");
			}

			if (moduleConfig.target == null) {
				throw new CompilerException("For module " + moduleName + " not specified target");
			}
			if (!moduleConfig.target.isAbsolute()) {
				moduleConfig.target = baseDir.resolve(moduleConfig.target);
			}
		}

		run(config);
	}

	public static void run(CompilerConfig config) throws CompilerException {
		Model model = parseModel(config.source);

		Collection<Module> modules = new ArrayList<>();

		for (Map.Entry<String, CompilerConfig.ModuleConfig> entry : config.modules.entrySet()) {
			final CompilerConfig.ModuleConfig value = entry.getValue();
			modules.add(new Module(entry.getKey(), value.lang, value.pack, value.target, value.api));
		}

		compile(modules, model);
	}

	public static Model parseModel(Path source) throws CompilerException {
		if (source == null || !Files.exists(source)) {
			throw new CompilerException("Invalid source path (" + source + ")");
		}

		final Parser parser = new Parser();

		for (File file : source.toFile().listFiles((dir, name) -> name.endsWith(".bdm"))) {
			try {
				parser.parse(TokenStream.read(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)));
			}
			catch (ParserException | IOException e) {
				throw new CompilerException("Error on parse model file " + file, e);
			}
		}

		try {
			return parser.buildModel();
		}
		catch (ParserException e) {
			throw new CompilerException("Error on build model", e);
		}
	}

	private static void compile(Collection<Module> modules, Model model) throws CompilerException {
		for (Module module : modules) {
			clearDirectory(module.getTarget().resolve(module.getPackage().replace('.', File.separatorChar)).toFile());
			ModuleBuilderManager.getBuilder(module.getLanguage()).build(module, model);
		}
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

	private Compiler() {}
}
