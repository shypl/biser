package org.shypl.biser.compiler;

import java.nio.file.Path;

public class CompilerConfig {
	public Path         source;
	public String       sourceCode;
	public ModuleConfig server;
	public ModuleConfig client;

	public static class ModuleConfig {
		public String lang;
		public String pack;
		public Path   target;
	}
}
