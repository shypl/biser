package org.shypl.biser.compiler.builder.actionscript;

import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.builder.OopCodeFile;
import org.shypl.biser.compiler.builder.OopModuleBuilder;

public class AsModuleBuilder extends OopModuleBuilder {
	public AsModuleBuilder() {
		super("actionscript", "as");
	}

	@Override
	protected OopCodeBuilder createCodeBuilder(String pack) {
		return new AsCodeBuilder(pack);
	}

	@Override
	protected OopCodeFile createCodeFile() {
		return new AsCodeFile();
	}
}
