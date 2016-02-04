package org.shypl.biser.compiler.code;

public class CodeStatementReturn extends CodeStatementExpression {
	public static final CodeStatementReturn EMPTY = new CodeStatementReturn();

	public CodeStatementReturn() {
		super(null);
	}

	public CodeStatementReturn(CodeExpression expression) {
		super(expression);
	}

	public CodeStatementReturn(String word) {
		super(new CodeExpressionWord(word));
	}

	public boolean isEmpty() {
		return getExpression() == null;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementReturn(this);
	}
}
