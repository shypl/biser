package org.shypl.biser.compiler.code;

public class CodeExpressionString implements CodeExpression {

	private final String string;

	public CodeExpressionString(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionString(this);
	}
}
