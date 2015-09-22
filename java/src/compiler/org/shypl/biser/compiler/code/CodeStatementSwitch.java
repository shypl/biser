package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CodeStatementSwitch implements CodeStatement {
	private final CodeExpression value;
	private final List<CodeStatementSwitchCase> cases = new ArrayList<>();
	private CodeStatementBlock defaultCase;

	public CodeStatementSwitch(CodeExpression value) {
		this.value = value;
	}

	public Collection<CodeStatementSwitchCase> getCases() {
		return cases;
	}

	public CodeStatementSwitchCase addCase(String condition) {
		return addCase(new CodeExpressionWord(condition));
	}

	public CodeStatementSwitchCase addCase(CodeExpression condition) {
		final CodeStatementSwitchCase c = new CodeStatementSwitchCase(condition);
		cases.add(c);
		return c;
	}

	public boolean hasDefaultCase() {
		return defaultCase != null;
	}

	public CodeStatementBlock getDefaultCase() {
		if (defaultCase == null) {
			defaultCase = new CodeStatementBlock();
			defaultCase.setBrackets(false);
		}
		return defaultCase;
	}

	public CodeExpression getValue() {
		return value;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitStatementSwitch(this);
	}
}
