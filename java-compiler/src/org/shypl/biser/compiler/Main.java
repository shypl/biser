package org.shypl.biser.compiler;

import java.io.File;

public class Main
{
	public static void main(final String[] args) throws ClassNotFoundException
	{
		try {
			final File config = new File(args.length == 0 ? "biser.properties" : args[0]);
			System.out.println("Start compilation (config: " + config.getAbsolutePath() + ")");
			Compiler.compile(config);
		}
		catch (CompilerException e) {
			System.out.println("ERROR: " + e.getMessage());
			System.exit(1);
		}
		catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}

		System.out.println("Complete");
	}
}
