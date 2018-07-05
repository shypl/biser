package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CodeExpressionCallClass implements CodeExpression {

	private final CodeType type;
	private final Collection<CodeExpression> arguments = new ArrayList<>();

	public CodeExpressionCallClass(CodeType type, CodeExpression... arguments) {
		this.type = type;
		Collections.addAll(this.arguments, arguments);
	}

	public CodeType getType() {
		return type;
	}

	public void addArgument(CodeExpression argument) {
		arguments.add(argument);
	}

	public Collection<CodeExpression> getArguments() {
		return new ArrayList<>(arguments);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionCallClass(this);
	}
}
