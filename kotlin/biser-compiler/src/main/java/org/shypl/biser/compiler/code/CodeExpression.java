package org.shypl.biser.compiler.code;

public interface CodeExpression extends CodeVisitable {
	default CodeExpressionField field(String name) {
		return new CodeExpressionField(this, name);
	}

	default CodeExpressionAssign assign(CodeExpression value) {
		return new CodeExpressionAssign(this, value);
	}

	default CodeExpressionAssign assign(String value) {
		return assign(new CodeExpressionWord(value));
	}

	default CodeExpressionMethod method(String name, CodeExpression... arguments) {
		return new CodeExpressionMethod(this, name, arguments);
	}
}
