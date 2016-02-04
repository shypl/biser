package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CodeExpressionMethod implements CodeExpression {
	private final CodeExpression target;
	private final String         method;
	private final Collection<CodeExpression> arguments = new ArrayList<>();

	public CodeExpressionMethod(String method, CodeExpression... arguments) {
		this(null, method, arguments);
	}

	public CodeExpressionMethod(CodeExpression target, String method, CodeExpression... arguments) {
		this.target = target;
		this.method = method;
		Collections.addAll(this.arguments, arguments);
	}

	public boolean hasTarget() {
		return target != null;
	}

	public CodeExpression getTarget() {
		return target;
	}

	public void addArgument(CodeExpression argument) {
		arguments.add(argument);
	}

	public void addArgument(String argument) {
		arguments.add(new CodeExpressionWord(argument));
	}

	public String getMethod() {
		return method;
	}

	public Collection<CodeExpression> getArguments() {
		return new ArrayList<>(arguments);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionMethod(this);
	}
}
