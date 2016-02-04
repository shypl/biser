package org.shypl.biser.compiler.code;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CodeParametrizedClass extends CodeType {
	private final CodeClass            ownerClass;
	private final Collection<CodeType> parameters;

	public CodeParametrizedClass(CodeClass ownerClass, CodeType... parameters) {
		super(ownerClass.getName() + "<" + StringUtils.join(parameters, ',') + ">");
		this.ownerClass = ownerClass;
		this.parameters = Arrays.asList(parameters);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitTypeParametrizedClass(this);
	}

	public CodeClass getOwnerClass() {
		return ownerClass;
	}

	public Collection<CodeType> getParameters() {
		return new ArrayList<>(parameters);
	}
}
