package org.shypl.biser.compiler.code;

public class CodeExpressionVar implements CodeExpression {
	private final String name;
	private final CodeType type;

	public CodeExpressionVar(String name, CodeType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public CodeExpressionWord getVariable() {
		return new CodeExpressionWord(name);
	}

	public CodeExpression getType() {
		return type;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionVar(this);
	}
}
