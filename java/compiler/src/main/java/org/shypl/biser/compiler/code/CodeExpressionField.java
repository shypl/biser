package org.shypl.biser.compiler.code;

public class CodeExpressionField implements CodeExpression {
	private final CodeExpression target;
	private final String         field;

	public CodeExpressionField(CodeExpression target, String field) {
		this.target = target;
		this.field = field;
	}

	public CodeExpression getTarget() {
		return target;
	}

	public String getField() {
		return field;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionField(this);
	}
}
