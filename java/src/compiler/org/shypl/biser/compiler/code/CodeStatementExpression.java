package org.shypl.biser.compiler.code;

public class CodeStatementExpression implements CodeStatement {
	private final CodeExpression expression;

	public CodeStatementExpression(CodeExpression expression) {
		this.expression = expression;
	}

	public CodeExpression getExpression() {
		return expression;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementExpression(this);
	}
}
