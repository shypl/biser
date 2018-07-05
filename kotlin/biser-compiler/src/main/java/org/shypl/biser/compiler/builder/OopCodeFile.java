package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.code.CodeClass;

public abstract class OopCodeFile extends CodeFile {
	public abstract void writeMainClass(CodeClass cls);
}
