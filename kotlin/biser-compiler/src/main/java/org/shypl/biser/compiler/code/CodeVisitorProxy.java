package org.shypl.biser.compiler.code;

public class CodeVisitorProxy implements CodeVisitor {

	private final CodeVisitor target;

	public CodeVisitorProxy(CodeVisitor target) {
		this.target = target;
	}

	@Override
	public void visit(CodeVisitable obj) {
		target.visit(obj);
	}

	@Override
	public void visitClass(CodeClass cls) {
		target.visitClass(cls);
	}

	@Override
	public void visitParameter(CodeParameter parameter) {
		target.visitParameter(parameter);
	}

	@Override
	public void visitMethod(CodeMethod method) {
		target.visitMethod(method);
	}

	@Override
	public void visitTypePrimitive(CodePrimitive type) {
		target.visitTypePrimitive(type);
	}

	@Override
	public void visitTypeArray(CodeArray type) {
		target.visitTypeArray(type);
	}

	@Override
	public void visitTypeClass(CodeClass type) {
		target.visitTypeClass(type);
	}

	@Override
	public void visitTypeParametrizedClass(CodeParametrizedClass type) {
		target.visitTypeParametrizedClass(type);
	}

	@Override
	public void visitTypeGeneric(CodeGeneric type) {
		target.visitTypeGeneric(type);
	}

	@Override
	public void visitStatementBlock(CodeStatementBlock block) {
		target.visitStatementBlock(block);
	}

	@Override
	public void visitStatementExpression(CodeStatementExpression statement) {
		target.visitStatementExpression(statement);
	}

	@Override
	public void visitStatementReturn(CodeStatementReturn statement) {
		target.visitStatementReturn(statement);
	}

	@Override
	public void visitStatementThrow(CodeStatementThrow statement) {
		target.visitStatementThrow(statement);
	}

	@Override
	public void visitStatementSwitch(CodeStatementSwitch statement) {
		target.visitStatementSwitch(statement);
	}

	@Override
	public void visitStatementSwitchCase(CodeStatementSwitchCase statement) {
		target.visitStatementSwitchCase(statement);
	}

	@Override
	public void visitStatementBreak(CodeStatementBreak statement) {
		target.visitStatementBreak(statement);
	}

	@Override
	public void visitStatementIf(CodeStatementIf statement) {
		target.visitStatementIf(statement);
	}

	@Override
	public void visitExpressionWord(CodeExpressionWord expression) {
		target.visitExpressionWord(expression);
	}

	@Override
	public void visitExpressionField(CodeExpressionField expression) {
		target.visitExpressionField(expression);
	}

	@Override
	public void visitExpressionMethod(CodeExpressionMethod expression) {
		target.visitExpressionMethod(expression);
	}

	@Override
	public void visitExpressionAssign(CodeExpressionAssign expression) {
		target.visitExpressionAssign(expression);
	}

	@Override
	public void visitExpressionNew(CodeExpressionNew expression) {
		target.visitExpressionNew(expression);
	}

	@Override
	public void visitExpressionNewArray(CodeExpressionNewArray expression) {
		target.visitExpressionNewArray(expression);
	}

	@Override
	public void visitExpressionStringConcat(CodeExpressionStringConcat expression) {
		target.visitExpressionStringConcat(expression);
	}

	@Override
	public void visitExpressionString(CodeExpressionString expression) {
		target.visitExpressionString(expression);
	}

	@Override
	public void visitExpressionVar(CodeExpressionVar expression) {
		target.visitExpressionVar(expression);
	}

	@Override
	public void visitExpressionBinaryOperator(CodeExpressionBinaryOperator expression) {
		target.visitExpressionBinaryOperator(expression);
	}

	@Override
	public void visitExpressionTernaryOperator(CodeExpressionTernaryOperator expression) {
		target.visitExpressionTernaryOperator(expression);
	}

	@Override
	public void visitExpressionCallClass(CodeExpressionCallClass expression) {
		target.visitExpressionCallClass(expression);
	}

	@Override
	public void visitExpressionLambda(CodeExpressionLambda expression) {
		target.visitExpressionLambda(expression);
	}
}
