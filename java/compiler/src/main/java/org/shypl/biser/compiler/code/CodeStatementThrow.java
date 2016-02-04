package org.shypl.biser.compiler.code;

public class CodeStatementThrow extends CodeStatementExpression {
	public CodeStatementThrow(CodeExpression expression) {
		super(expression);
	}

	public CodeStatementThrow(String expressionWord) {
		this(new CodeExpressionWord(expressionWord));
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementThrow(this);
	}
}
