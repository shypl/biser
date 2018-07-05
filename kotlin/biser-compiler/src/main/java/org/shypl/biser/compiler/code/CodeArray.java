package org.shypl.biser.compiler.code;

public class CodeArray extends CodeType {
	private final CodeType elementType;

	CodeArray(CodeType elementType) {
		super(elementType.getName() + "[]");
		this.elementType = elementType;
	}

	public CodeType getElementType() {
		return elementType;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitTypeArray(this);
	}
}
