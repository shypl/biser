package org.shypl.biser.compiler.builder.flash;

import org.shypl.biser.compiler.builder.OopCodeFile;
import org.shypl.biser.compiler.builder.UsedClasses;
import org.shypl.biser.compiler.code.CodeArray;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeExpressionAssign;
import org.shypl.biser.compiler.code.CodeExpressionBinaryOperator;
import org.shypl.biser.compiler.code.CodeExpressionCallClass;
import org.shypl.biser.compiler.code.CodeExpressionField;
import org.shypl.biser.compiler.code.CodeExpressionMethod;
import org.shypl.biser.compiler.code.CodeExpressionNew;
import org.shypl.biser.compiler.code.CodeExpressionNewArray;
import org.shypl.biser.compiler.code.CodeExpressionString;
import org.shypl.biser.compiler.code.CodeExpressionStringConcat;
import org.shypl.biser.compiler.code.CodeExpressionTernaryOperator;
import org.shypl.biser.compiler.code.CodeExpressionVar;
import org.shypl.biser.compiler.code.CodeExpressionWord;
import org.shypl.biser.compiler.code.CodeGeneric;
import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.CodeModifier;
import org.shypl.biser.compiler.code.CodeParameter;
import org.shypl.biser.compiler.code.CodeParametrizedClass;
import org.shypl.biser.compiler.code.CodePrimitive;
import org.shypl.biser.compiler.code.CodeStatement;
import org.shypl.biser.compiler.code.CodeStatementBlock;
import org.shypl.biser.compiler.code.CodeStatementBreak;
import org.shypl.biser.compiler.code.CodeStatementExpression;
import org.shypl.biser.compiler.code.CodeStatementIf;
import org.shypl.biser.compiler.code.CodeStatementReturn;
import org.shypl.biser.compiler.code.CodeStatementSwitch;
import org.shypl.biser.compiler.code.CodeStatementSwitchCase;
import org.shypl.biser.compiler.code.CodeStatementThrow;
import org.shypl.biser.compiler.code.CodeType;
import org.shypl.biser.compiler.code.CodeVisitor;

import java.util.Collection;

public class FlashCodeFile extends OopCodeFile implements CodeVisitor {
	private UsedClasses usedClasses;

	@Override
	public void writeMainClass(CodeClass cls) {
		usedClasses = new UsedClasses(cls.getPackage(), true);
		usedClasses.use(cls);

		writeLine("package ", cls.getPackage().getFullName('.'), " {");
		addTab();

		writeImports();

		writeClass(cls);

		removeTab();
		writeLine("}");

		if (cls.hasClasses()) {
			usedClasses = new UsedClasses(null, true);
			for (CodeClass innerClass : cls.getClasses()) {
				usedClasses.use(innerClass);
			}

			writeLine();
			writeImports();

			for (CodeClass innerClass : cls.getClasses()) {
				writeClass(innerClass);
			}
		}

		usedClasses = null;
	}

	private void writeImports() {
		Collection<CodeClass> importedClasses = usedClasses.getImportedClasses();
		if (!importedClasses.isEmpty()) {
			for (CodeClass importedClass : importedClasses) {
				writeLine("import ", importedClass.getFullName(), ";");
			}
			writeLine();
		}
	}

	@Override
	public void visitTypePrimitive(CodePrimitive type) {
		write(type.getName());
	}

	@Override
	public void visitTypeClass(CodeClass type) {
		write(usedClasses.getDeclaration(type));
	}

	@Override
	public void visitTypeArray(CodeArray type) {
		write("Vector.<");
		type.getElementType().visit(this);
		write(">");
	}

	@Override
	public void visitTypeParametrizedClass(CodeParametrizedClass type) {
		type.getOwnerClass().visit(this);
		write("<");
		writeSeparated(type.getParameters(), this::visit);
		write(">");
	}

	@Override
	public void visitTypeGeneric(CodeGeneric type) {
		write(type.getName());
	}

	@Override
	public void visitStatementBlock(CodeStatementBlock block) {
		if (block.isEmpty()) {
			if (block.hasBrackets()) {
				writeLine("{}");
			}
		}
		else {
			if (block.hasBrackets()) {
				writeLine("{");
			}
			addTab();
			for (CodeStatement statement : block.getStatements()) {
				statement.visit(this);
			}
			removeTab();
			if (block.hasBrackets()) {
				writeLine("}");
			}
		}
	}

	@Override
	public void visitStatementExpression(CodeStatementExpression statement) {
		statement.getExpression().visit(this);
		writeLine(";");
	}

	@Override
	public void visitStatementReturn(CodeStatementReturn statement) {
		write("return ");
		visitStatementExpression(statement);
	}

	@Override
	public void visitStatementThrow(CodeStatementThrow statement) {
		write("throw ");
		visitStatementExpression(statement);
	}

	@Override
	public void visitStatementSwitch(CodeStatementSwitch statement) {
		write("switch (");
		statement.getValue().visit(this);
		writeLine(") {");
		addTab();

		for (CodeStatementSwitchCase cs : statement.getCases()) {
			cs.visit(this);
		}

		if (statement.hasDefaultCase()) {
			CodeStatementBlock defaultCase = statement.getDefaultCase();
			writeLine("default:");
			defaultCase.visit(this);
		}

		removeTab();
		writeLine("}");
	}

	@Override
	public void visitStatementSwitchCase(CodeStatementSwitchCase statement) {
		write("case ");
		statement.getCondition().visit(this);
		writeLine(":");
		visitStatementBlock(statement);
	}

	@Override
	public void visitStatementBreak(CodeStatementBreak statement) {
		writeLine("break;");
	}

	@Override
	public void visitStatementIf(CodeStatementIf statement) {
		write("if (");
		statement.getCondition().visit(this);
		write(") ");
		visitStatementBlock(statement);
	}

	@Override
	public void visitExpressionWord(CodeExpressionWord expression) {
		write(expression.getWord());
	}

	@Override
	public void visitExpressionField(CodeExpressionField expression) {
		expression.getTarget().visit(this);
		write(".", expression.getField());
	}

	@Override
	public void visitExpressionMethod(CodeExpressionMethod expression) {
		if (expression.hasTarget()) {
			expression.getTarget().visit(this);
			write(".");
		}
		write(expression.getMethod(), "(");
		writeSeparated(expression.getArguments(), this::visit);
		write(")");
	}

	@Override
	public void visitExpressionAssign(CodeExpressionAssign expression) {
		expression.getTarget().visit(this);
		write(" = ");
		expression.getValue().visit(this);
	}

	@Override
	public void visitExpressionNew(CodeExpressionNew expression) {
		write("new ");
		expression.getType().visit(this);
		write("(");
		writeSeparated(expression.getArguments(), this::visit);
		write(")");
	}

	@Override
	public void visitExpressionStringConcat(CodeExpressionStringConcat expression) {
		writeSeparated(expression.getExpressions(), this::visit, " + ");
	}

	@Override
	public void visitExpressionString(CodeExpressionString expression) {
		write("\"", expression.getString().replaceAll("\"", "\\\\\""), "\"");
	}

	@Override
	public void visitExpressionNewArray(CodeExpressionNewArray expression) {
		expression.getType().getElementType().visit(this);
		if (expression.isSized()) {
			write("new Array(", String.valueOf(expression.getSize()), ")");
		}
		else {
			write("[");
			writeSeparated(expression.getElements(), this::visit);
			write("]");
		}
	}

	@Override
	public void visitExpressionVar(CodeExpressionVar expression) {
		write("var ", expression.getName(), ":");
		expression.getType().visit(this);
	}

	@Override
	public void visitExpressionBinaryOperator(CodeExpressionBinaryOperator expression) {
		expression.getLeft().visit(this);
		write(" ", expression.getOperator(), " ");
		expression.getRight().visit(this);
	}

	@Override
	public void visitExpressionTernaryOperator(CodeExpressionTernaryOperator expression) {
		expression.getCondition().visit(this);
		write(" ? ");
		expression.getTrueExp().visit(this);
		write(" : ");
		expression.getFalseExp().visit(this);
	}

	@Override
	public void visitExpressionCallClass(CodeExpressionCallClass expression) {
		expression.getType().visit(this);
		write("(");
		writeSeparated(expression.getArguments(), this::visit);
		write(")");
	}

	private void writeClass(CodeClass cls) {
		writeModifier(cls.getModifier());

		write(cls.isInterface() ? "interface " : "class ", cls.getName());

		if (cls.hasParent()) {
			write(" extends ");
			writeType(cls.getParent());
		}

		if (cls.hasImplements()) {
			write(cls.isInterface() ? " extends " : " implements ");
			writeSeparated(cls.getImplements(), this::writeType);
		}

		writeLine(" {");
		addTab();

		if (cls.hasFields()) {
			for (CodeParameter field : cls.getFields()) {
				writeField(field);
			}
			writeLine();
		}

		writeSeparated(cls.getMethods(), this::writeMethod, this::writeLine);

		removeTab();
		writeLine("}");
	}

	private void writeField(CodeParameter field) {
		writeModifier(field.getModifier());
		write(field.getModifier().is(CodeModifier.CONST) ? "const " : "var ");
		write(field.getName(), ":");
		writeType(field.getType());
		if (field.hasValue()) {
			write(" = ");
			field.getValue().visit(this);
		}
		writeLine(";");
	}

	private void writeMethod(CodeMethod method) {
		writeModifier(method.getModifier());

		write("function ");
		if (method.getModifier().is(CodeModifier.GETTER)) {
			write("get ");
		}
		else if(method.getModifier().is(CodeModifier.SETTER)) {
			write("set ");
		}
		write(method.getName(), "(");
		writeSeparated(method.getArguments(), arg -> {
			write(arg.getName(), ":");
			writeType(arg.getType());
			if (arg.hasValue()) {
				write(" = ");
				arg.getValue().visit(this);
			}
		});
		write(")");

		if (method.hasReturnType()) {
			write(":");
			writeType(method.getReturnType());
		}
		if (method.getModifier().is(CodeModifier.INTERFACE)) {
			writeLine(";");
		}
		else {
			write(" ");
			method.getBody().visit(this);
		}
	}

	private void writeModifier(CodeModifier modifier) {
		if (modifier.is(CodeModifier.ABSTRACT)) {
			writeLine("[Abstract]");
		}
		if (modifier.is(CodeModifier.OVERRIDE)) {
			write("override ");
		}
		if (modifier.is(CodeModifier.PUBLIC)) {
			write("public ");
		}
		if (modifier.is(CodeModifier.PROTECTED)) {
			write("protected ");
		}
		if (modifier.is(CodeModifier.PRIVATE)) {
			write("private ");
		}
		if (modifier.is(CodeModifier.INTERNAL)) {
			write("internal ");
		}
		if (modifier.is(CodeModifier.STATIC)) {
			write("static ");
		}
		if (modifier.is(CodeModifier.FINAL)) {
			write("final ");
		}
	}

	private void writeType(CodeType type) {
		type.visit(this);
	}
}
