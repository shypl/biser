package org.shypl.biser.compiler.builder.kotlin.js;

import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.builder.OopCodeFile;
import org.shypl.biser.compiler.builder.OopModuleBuilder;

public class KotlinJsModuleBuilder extends OopModuleBuilder {
	public KotlinJsModuleBuilder() {
		super("kotlin-js", "kt");
	}

	@Override
	protected OopCodeBuilder createCodeBuilder(String pack) {
		return new KotlinJsCodeBuilder(pack);
	}

	@Override
	protected OopCodeFile createCodeFile() {
		return new KotlinJsCodeFile();
	}
}
