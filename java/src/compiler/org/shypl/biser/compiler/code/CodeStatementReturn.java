package org.shypl.biser.compiler.code;

public class CodeStatementReturn extends CodeStatementExpression {
	public CodeStatementReturn(CodeExpression expression) {
		super(expression);
	}

	public CodeStatementReturn(String word) {
		super(new CodeExpressionWord(word));
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementReturn(this);
	}
}
