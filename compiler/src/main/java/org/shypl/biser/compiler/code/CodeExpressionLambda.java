package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CodeExpressionLambda implements CodeExpression {

	private final Collection<String> arguments = new ArrayList<>();
	private final CodeStatementBlock         body      = new CodeStatementBlock();

	public CodeExpressionLambda(String... arguments) {
		Collections.addAll(this.arguments, arguments);
	}

	public List<String> getArguments() {
		return new ArrayList<>(arguments);
	}

	public CodeStatementBlock getBody() {
		return body;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionLambda(this);
	}
}
