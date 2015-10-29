package org.shypl.biser.compiler.code;

public class CodeParameter extends CodeNamedObject implements CodeModifiable, CodeVisitable {
	private final CodeModifier modifier = new CodeModifier();
	private CodeType       type;
	private CodeExpression value;

	CodeParameter(String name) {
		super(name);
	}

	@Override
	public CodeModifier getModifier() {
		return modifier;
	}

	public CodeParameter setType(CodeType type) {
		this.type = type;
		return this;
	}

	public CodeType getType() {
		return type;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitParameter(this);
	}

	public void setValue(CodeExpression value) {
		this.value = value;
	}

	public CodeExpression getValue() {
		return value;
	}

	public boolean hasValue() {
		return value != null;
	}

	public CodeExpression getVariable() {
		return new CodeExpressionWord(getName());
	}
}
