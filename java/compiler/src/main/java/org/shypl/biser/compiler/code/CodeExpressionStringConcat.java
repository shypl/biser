package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CodeExpressionStringConcat implements CodeExpression {
	private final List<CodeExpression> expressions = new ArrayList<>();

	public CodeExpressionStringConcat(CodeExpression... expressions) {
		Collections.addAll(this.expressions, expressions);
	}

	public List<CodeExpression> getExpressions() {
		return new ArrayList<>(expressions);
	}

	public void addExpression(CodeExpression expression) {
		expressions.add(expression);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionStringConcat(this);
	}
}
