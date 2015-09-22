package org.shypl.biser.compiler.code;

public class CodeExpressionAssign implements CodeExpression {
	private final CodeExpression target;
	private final CodeExpression value;

	public CodeExpressionAssign(CodeExpression target, CodeExpression value) {
		this.target = target;
		this.value = value;
	}

	public CodeExpression getTarget() {
		return target;
	}

	public CodeExpression getValue() {
		return value;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionAssign(this);
	}
}
