package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CodeExpressionNewArray implements CodeExpression {

	private final CodeArray type;
	private final int size;
	private final Collection<CodeExpression> elements = new ArrayList<>();

	public CodeExpressionNewArray(CodeArray type, int size) {
		this.type = type;
		this.size = size;
	}

	public CodeExpressionNewArray(CodeArray type, CodeExpression... elements) {
		this.type = type;
		this.size = -1;
		Collections.addAll(this.elements, elements);
	}

	public CodeArray getType() {
		return type;
	}

	public void addElement(CodeExpression element) {
		elements.add(element);
	}

	public Collection<CodeExpression> getElements() {
		return new ArrayList<>(elements);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionNewArray(this);
	}

	public int getSize() {
		return size;
	}

	public boolean isSized() {
		return size != -1;
	}
}
