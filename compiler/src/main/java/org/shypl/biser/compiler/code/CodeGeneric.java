package org.shypl.biser.compiler.code;

public class CodeGeneric extends CodeType {
	private Dependence dependence;
	private CodeType   dependenceType;

	public CodeGeneric(String name) {
		super(name);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitTypeGeneric(this);
	}

	public boolean hasDependence() {
		return dependence != null;
	}

	public Dependence getDependence() {
		return dependence;
	}

	public CodeType getDependenceType() {
		return dependenceType;
	}

	public void setDependence(Dependence dependence, CodeType dependenceType) {
		this.dependence = dependence;
		this.dependenceType = dependenceType;
	}

	public enum Dependence {EXTENDS, SUPER}
}
