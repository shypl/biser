package org.shypl.biser.compiler.code;

public class CodeExpressionTernaryOperator implements CodeExpression {
	private final CodeExpression condition;
	private final CodeExpression trueExp;
	private final CodeExpression falseExp;

	public CodeExpressionTernaryOperator(CodeExpression condition, CodeExpression trueExp, CodeExpression falseExp) {
		this.condition = condition;
		this.trueExp = trueExp;
		this.falseExp = falseExp;
	}

	public CodeExpression getCondition() {
		return condition;
	}

	public CodeExpression getTrueExp() {
		return trueExp;
	}

	public CodeExpression getFalseExp() {
		return falseExp;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionTernaryOperator(this);
	}
}
