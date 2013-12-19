package org.shypl.biser.compiler;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;

public class Main
{
	public static void main(String[] args)
	{
		if (args.length == 0) {
			System.out.println("Required config file argument");
			return;
		}

		new Main(args[0]);
	}

	public Main(String configFile)
	{
		try {

			final File file = new File(configFile);

			System.out.println("Load config " + file);

			final Config config = (Config)new Yaml(new Constructor(Config.class)).load(new FileInputStream(file));
			final Compiler compiler = new Compiler();

			Path root = file.getAbsoluteFile().getParentFile().toPath();

			System.out.println("Working directory " + root);

			compiler.load(root.resolve(config.src).toRealPath());

			for (Map.Entry<String, Config.Target> entry : config.target.entrySet()) {
				Config.Target target = entry.getValue();
				compiler.compile(Lang.valueOf(entry.getKey().toUpperCase()), root.resolve(target.path).toRealPath(), target.pkg);
			}

			System.out.println("Complete");
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}

	private static class Config
	{
		public Map<String, Target> target;
		public String              src;

		public static class Target
		{
			public String path;
			public String pkg;
		}
	}
}
