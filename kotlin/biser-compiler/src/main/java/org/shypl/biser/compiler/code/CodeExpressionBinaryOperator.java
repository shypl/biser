package org.shypl.biser.compiler.code;

public class CodeExpressionBinaryOperator implements CodeExpression {
	private final String         operator;
	private final CodeExpression left;
	private final CodeExpression right;

	public CodeExpressionBinaryOperator(String operator, CodeExpression left, CodeExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public CodeExpression getLeft() {
		return left;
	}

	public CodeExpression getRight() {
		return right;
	}

	public String getOperator() {
		return operator;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionBinaryOperator(this);
	}
}
