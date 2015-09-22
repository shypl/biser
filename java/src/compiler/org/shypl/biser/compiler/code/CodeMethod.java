package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CodeMethod extends CodeNamedObject implements CodeModifiable, CodeVisitable {
	private final CodeNamedObjectSet<CodeParameter> arguments     = new CodeNamedObjectSet<>();
	private final CodeModifier                      modifier      = new CodeModifier();
	private final List<CodeClass>                   throwsClasses = new ArrayList<>();
	private final CodeStatementBlock                body          = new CodeStatementBlock();
	private CodeType returnType;

	public CodeMethod(String name) {
		super(name);
	}

	@Override
	public CodeModifier getModifier() {
		return modifier;
	}

	public void setReturnType(CodeType returnType) {
		this.returnType = returnType;
	}

	public boolean hasReturnType() {
		return returnType != null;
	}

	public CodeType getReturnType() {
		return returnType;
	}

	public CodeParameter getArgument(String name) {
		CodeParameter argument = arguments.get(name);
		if (argument == null) {
			argument = arguments.add(new CodeParameter(name));
		}
		return argument;
	}

	public Collection<CodeParameter> getArguments() {
		return arguments.getElements();
	}

	public CodeStatementBlock getBody() {
		return body;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitMethod(this);
	}

	public void addThrows(CodeClass cls) {
		throwsClasses.add(cls);
	}

	public List<CodeClass> getThrows() {
		return new ArrayList<>(throwsClasses);
	}

	public boolean hasThrows() {
		return !throwsClasses.isEmpty();
	}
}
