package org.shypl.biser.compiler.code;

public class CodePrimitive extends CodeType {
	CodePrimitive(String name) {
		super(name);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitTypePrimitive(this);
	}
}
