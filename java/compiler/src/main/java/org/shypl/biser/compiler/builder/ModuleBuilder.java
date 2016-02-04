package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.CompilerException;
import org.shypl.biser.compiler.Module;
import org.shypl.biser.compiler.model.Model;

public interface ModuleBuilder {
	String getLang();

	void build(Module module, Model model) throws CompilerException;
}
