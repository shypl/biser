package org.shypl.biser.compiler.code;

public abstract class CodeType extends CodeNamedObject implements CodeVisitable, CodeExpression {
	public CodeType(String name) {
		super(name);
	}
}
