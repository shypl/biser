package org.shypl.biser.compiler.code;

public class CodeStatementSwitchCase extends CodeStatementBlock {
	private final CodeExpression condition;

	public CodeStatementSwitchCase(final CodeExpression condition) {
		this.condition = condition;
		setBrackets(false);
	}

	public CodeExpression getCondition() {
		return condition;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementSwitchCase(this);
	}
}
