package org.shypl.biser.compiler.builder.java;

import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.builder.OopCodeFile;
import org.shypl.biser.compiler.builder.OopModuleBuilder;

public class JavaModuleBuilder extends OopModuleBuilder {

	public JavaModuleBuilder() {
		super("java", "java");
	}

	@Override
	protected OopCodeBuilder createCodeBuilder(String pack) {
		return new JavaCodeBuilder(pack);
	}

	@Override
	protected OopCodeFile createCodeFile() {
		return new JavaCodeFile();
	}
}
