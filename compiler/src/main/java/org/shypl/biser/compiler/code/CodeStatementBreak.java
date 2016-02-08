package org.shypl.biser.compiler.code;

public class CodeStatementBreak implements CodeStatement {

	public static final CodeStatementBreak INSTANCE = new CodeStatementBreak();

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementBreak(this);
	}
}
