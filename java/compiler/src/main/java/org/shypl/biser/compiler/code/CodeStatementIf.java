package org.shypl.biser.compiler.code;

public class CodeStatementIf extends CodeStatementBlock {
	private final CodeExpression condition;

	public CodeStatementIf(CodeExpression condition) {
		this.condition = condition;
	}

	public CodeExpression getCondition() {
		return condition;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementIf(this);
	}
}
