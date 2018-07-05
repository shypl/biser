package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class CodeExpressionNew implements CodeExpression {

	private final CodeType type;
	private final Collection<CodeExpression> arguments = new ArrayList<>();
	private final Collection<CodeMethod>     methods   = new LinkedList<>();

	public CodeExpressionNew(CodeType type, CodeExpression... arguments) {
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

	public boolean isInlineDeclaration() {
		return hasMethods();
	}

	public CodeMethod addMethod(String name) {
		CodeMethod method = new CodeMethod(name);
		methods.add(method);
		return method;
	}

	public Collection<CodeMethod> getMethods() {
		return new ArrayList<>(methods);
	}

	public boolean hasMethods() {
		return !methods.isEmpty();
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionNew(this);
	}
}
