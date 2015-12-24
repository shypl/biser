package org.shypl.biser.compiler;

import org.shypl.biser.compiler.model.ApiSide;

import java.nio.file.Path;
import java.util.Map;

public class CompilerConfig {
	public Path                      source;
	public String                    sourceCode;
	public Map<String, ModuleConfig> modules;

	public static class ModuleConfig {
		public String               lang;
		public String               pack;
		public Path                 target;
		public Map<String, ApiSide> api;
	}
}
