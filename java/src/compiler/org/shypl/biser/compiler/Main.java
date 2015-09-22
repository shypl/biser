package org.shypl.biser.compiler;

import java.nio.file.Paths;

public class Main
{
	public static void main(final String[] args)
	{
		try {
			Compiler.run(Paths.get(args.length == 0 ? "biser.yml" : args[0]));
		}
		catch (CompilerException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
