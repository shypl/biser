package org.shypl.biser.compiler.code;

public class CodeExpressionStatement implements CodeExpression {
	private final CodeStatement statement;

	public CodeExpressionStatement(CodeStatement statement) {
		this.statement = statement;
	}

	public CodeStatement getStatement() {
		return statement;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionStatement(this);
	}
}
