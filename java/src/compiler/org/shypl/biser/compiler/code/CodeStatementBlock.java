package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;

public class CodeStatementBlock implements CodeStatement {
	private final Collection<CodeStatement> statements = new ArrayList<>();
	private boolean brackets = true;

	public CodeStatementBlock addStatement(CodeStatement statement) {
		statements.add(statement);
		return this;
	}

	public CodeStatementBlock addStatement(CodeExpression expression) {
		statements.add(new CodeStatementExpression(expression));
		return this;
	}

	public Collection<CodeStatement> getStatements() {
		return new ArrayList<>(statements);
	}

	public boolean isEmpty() {
		return statements.isEmpty();
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementBlock(this);
	}

	public boolean hasBrackets() {
		return brackets;
	}

	public void setBrackets(boolean brackets) {
		this.brackets = brackets;
	}
}
