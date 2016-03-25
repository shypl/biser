package org.shypl.biser.compiler.code;

public interface CodeVisitor {
	default void visit(CodeVisitable obj) {
		obj.visit(this);
	}

	default void visitClass(CodeClass cls) {

		if (cls.hasGenerics()) {
			for (CodeGeneric generic : cls.getGenerics()) {
				generic.visit(this);
			}
		}

		if (cls.hasParent()) {
			cls.getParent().visit(this);
		}

		for (CodeClass impl : cls.getImplements()) {
			impl.visit(this);
		}

		for (CodeParameter field : cls.getFields()) {
			field.visit(this);
		}

		for (CodeMethod method : cls.getMethods()) {
			method.visit(this);
		}
	}

	default void visitParameter(CodeParameter parameter) {
		parameter.getType().visit(this);
		if (parameter.hasValue()) {
			parameter.getValue().visit(this);
		}
	}

	default void visitMethod(CodeMethod method) {
		if (method.hasReturnType()) {
			method.getReturnType().visit(this);
		}

		for (CodeClass cls : method.getThrows()) {
			cls.visit(this);
		}

		for (CodeParameter arg : method.getArguments()) {
			arg.visit(this);
		}

		method.getBody().visit(this);
	}

	default void visitTypePrimitive(CodePrimitive type) {
	}

	default void visitTypeArray(CodeArray type) {
		type.getElementType().visit(this);
	}

	default void visitTypeClass(CodeClass type) {
	}

	default void visitTypeParametrizedClass(CodeParametrizedClass type) {
		type.getOwnerClass().visit(this);
		for (CodeType parameter : type.getParameters()) {
			parameter.visit(this);
		}
	}

	default void visitTypeGeneric(CodeGeneric type) {
		if (type.hasDependence()) {
			type.getDependenceType().visit(this);
		}
	}

	default void visitStatementBlock(CodeStatementBlock block) {
		for (CodeStatement statement : block.getStatements()) {
			statement.visit(this);
		}
	}

	default void visitStatementExpression(CodeStatementExpression statement) {
		statement.getExpression().visit(this);
	}

	default void visitStatementReturn(CodeStatementReturn statement) {
		if (!statement.isEmpty()) {
			visitStatementExpression(statement);
		}
	}

	default void visitStatementThrow(CodeStatementThrow statement) {
		visitStatementExpression(statement);
	}

	default void visitStatementSwitch(CodeStatementSwitch statement) {
		statement.getValue().visit(this);
		for (CodeStatementSwitchCase cs : statement.getCases()) {
			cs.visit(this);
		}
		if (statement.hasDefaultCase()) {
			statement.getDefaultCase().visit(this);
		}
	}

	default void visitStatementSwitchCase(CodeStatementSwitchCase statement) {
		statement.getCondition().visit(this);
		visitStatementBlock(statement);
	}

	default void visitStatementBreak(CodeStatementBreak statement) {
	}

	default void visitStatementIf(CodeStatementIf statement) {
		statement.getCondition().visit(this);
		visitStatementBlock(statement);
	}

	default void visitExpressionWord(CodeExpressionWord expression) {
	}

	default void visitExpressionField(CodeExpressionField expression) {
		expression.getTarget().visit(this);
	}

	default void visitExpressionMethod(CodeExpressionMethod expression) {
		if (expression.hasTarget()) {
			expression.getTarget().visit(this);
		}
		for (CodeExpression exp : expression.getArguments()) {
			exp.visit(this);
		}
	}

	default void visitExpressionAssign(CodeExpressionAssign expression) {
		expression.getTarget().visit(this);
		expression.getValue().visit(this);
	}

	default void visitExpressionNew(CodeExpressionNew expression) {
		expression.getType().visit(this);
		for (CodeExpression arg : expression.getArguments()) {
			arg.visit(this);
		}

		if (expression.hasMethods()) {
			for (CodeMethod method : expression.getMethods()) {
				method.visit(this);
			}
		}
	}

	default void visitExpressionNewArray(CodeExpressionNewArray expression) {
		expression.getType().visit(this);
		for (CodeExpression arg : expression.getElements()) {
			arg.visit(this);
		}
	}

	default void visitExpressionStringConcat(CodeExpressionStringConcat expression) {
		for (CodeExpression e : expression.getExpressions()) {
			e.visit(this);
		}
	}

	default void visitExpressionString(CodeExpressionString expression) {
	}

	default void visitExpressionVar(CodeExpressionVar expression) {
		expression.getType().visit(this);
	}

	default void visitExpressionBinaryOperator(CodeExpressionBinaryOperator expression) {
		expression.getLeft().visit(this);
		expression.getRight().visit(this);
	}

	default void visitExpressionTernaryOperator(CodeExpressionTernaryOperator expression) {
		expression.getCondition().visit(this);
		expression.getTrueExp().visit(this);
		expression.getFalseExp().visit(this);
	}

	default void visitExpressionCallClass(CodeExpressionCallClass expression) {
		expression.getType().visit(this);
		for (CodeExpression arg : expression.getArguments()) {
			arg.visit(this);
		}
	}

	default void visitExpressionLambda(CodeExpressionLambda expression) {
		expression.getBody().visit(this);
	}
}
