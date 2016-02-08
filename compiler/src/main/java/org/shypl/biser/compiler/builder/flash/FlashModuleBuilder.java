package org.shypl.biser.compiler.builder.flash;

import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.builder.OopCodeFile;
import org.shypl.biser.compiler.builder.OopModuleBuilder;

public class FlashModuleBuilder extends OopModuleBuilder {
	public FlashModuleBuilder() {
		super("actionscript", "as");
	}

	@Override
	protected OopCodeBuilder createCodeBuilder(String pack) {
		return new FlashCodeBuilder(pack);
	}

	@Override
	protected OopCodeFile createCodeFile() {
		return new FlashCodeFile();
	}
}
