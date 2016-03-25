package org.shypl.biser.compiler.builder.java;

import org.shypl.biser.compiler.builder.OopCodeFile;
import org.shypl.biser.compiler.builder.UsedClasses;
import org.shypl.biser.compiler.code.CodeArray;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeExpressionAssign;
import org.shypl.biser.compiler.code.CodeExpressionBinaryOperator;
import org.shypl.biser.compiler.code.CodeExpressionField;
import org.shypl.biser.compiler.code.CodeExpressionLambda;
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
import org.shypl.biser.compiler.code.CodePackage;
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
import org.shypl.biser.compiler.code.CodeVisitorProxy;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("Duplicates")
public class JavaCodeFile extends OopCodeFile implements CodeVisitor {
	private UsedClasses usedClasses;
	private CodeVisitor inlineLambdaStatementVisitor = new LambdaVisitor(this);

	@Override
	public void writeMainClass(CodeClass cls) {
		usedClasses = new UsedClasses(cls.getPackage(), false);
		usedClasses.use(cls);
		usedClasses.ignoreImportPackage(cls.getEngine().getPackage("java.lang"));

		writeLine("package ", cls.getPackage().getFullName('.'), ";");
		writeLine();

		Collection<CodeClass> importedClasses = usedClasses.getImportedClasses(new Comparator<CodeClass>() {
			@Override
			public int compare(CodeClass o1, CodeClass o2) {
				CodePackage p1 = o1.getPackage().getFirstPackage();
				CodePackage p2 = o2.getPackage().getFirstPackage();
				if (p1 != p2) {
					if (p1.getName().equals("java")) {
						return 1;
					}
					if (p2.getName().equals("java")) {
						return -1;
					}
				}

				return 0;
			}
		});
		if (!importedClasses.isEmpty()) {
			CodePackage prevRootPackage = null;
			for (CodeClass importedClass : importedClasses) {
				CodePackage rootPackage = importedClass.getPackage().getFirstPackage();
				if (prevRootPackage != null && prevRootPackage != rootPackage) {
					writeLine();
				}
				prevRootPackage = rootPackage;
				writeLine("import ", importedClass.getFullName(), ";");
			}
			writeLine();
		}

		writeClass(cls);

		usedClasses = null;
	}

	@Override
	public void visitTypePrimitive(CodePrimitive type) {
		write(type.getName());
	}

	@Override
	public void visitTypeArray(CodeArray type) {
		type.getElementType().visit(this);
		write("[]");
	}

	@Override
	public void visitTypeClass(CodeClass type) {
		write(usedClasses.getDeclaration(type));
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
		if (statement.isEmpty()) {
			writeLine("return;");
		}
		else {
			write("return ");
			visitStatementExpression(statement);
		}
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
			boolean b = defaultCase.hasBrackets();
			if (b) {
				defaultCase.setBrackets(false);
				writeLine("default: {");
			}
			else {
				writeLine("default:");
			}
			defaultCase.visit(this);
			if (b) {
				defaultCase.setBrackets(true);
				writeLine("}");
			}
		}

		removeTab();
		writeLine("}");
	}

	@Override
	public void visitStatementSwitchCase(CodeStatementSwitchCase statement) {
		write("case ");
		statement.getCondition().visit(this);
		boolean b = statement.hasBrackets();
		if (b) {
			statement.setBrackets(false);
			writeLine(": {");
		}
		else {
			writeLine(":");
		}
		visitStatementBlock(statement);
		if (b) {
			statement.setBrackets(true);
			writeLine("}");
		}
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

		if (expression.isInlineDeclaration()) {
			writeLine(" {");
			addTab();
			writeSeparated(expression.getMethods(), this::writeMethod, this::writeLine);
			removeTab();
			write("}");
		}
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
		write("new ");
		expression.getType().getElementType().visit(this);
		if (expression.isSized()) {
			write("[", String.valueOf(expression.getSize()), "]");
		}
		else {
			write("[]{");
			writeSeparated(expression.getElements(), this::visit);
			write("}");
		}
	}

	@Override
	public void visitExpressionVar(CodeExpressionVar expression) {
		expression.getType().visit(this);
		write(" ", expression.getName());
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
	public void visitExpressionLambda(CodeExpressionLambda expression) {
		List<String> arguments = expression.getArguments();
		if (arguments.isEmpty()) {
			write("() -> ");
		}
		else if (arguments.size() == 1) {
			write(arguments.get(0), " -> ");
		}
		else {
			write("(");
			writeSeparated(arguments, this::write);
			write(") - >");
		}

		CodeStatementBlock block = expression.getBody();
		if (block.isEmpty()) {
			write("{}");
		}
		else if (block.countStatements() == 1) {
			block.getStatements().get(0).visit(inlineLambdaStatementVisitor);
		}
		else {
			writeLine("{");
			addTab();
			for (CodeStatement statement : block.getStatements()) {
				statement.visit(this);
			}
			removeTab();
			write("}");
		}
	}

	private void writeClass(CodeClass cls) {
		writeModifier(cls.getModifier());

		write(cls.isEnum() ? "enum " : (cls.isInterface() ? "interface " : "class "), cls.getName());

		if (cls.hasGenerics()) {
			write("<");
			writeSeparated(cls.getGenerics(), this::writeGenericDeclaration);
			write(">");
		}

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

		if (cls.isEnum()) {
			writeSeparated(cls.getEnumValues(), this::write, () -> {
				writeLine(",");
			});

			if (cls.hasFields() || cls.hasMethods()) {
				writeLine(";");
				writeLine();
			}
			else {
				writeLine();
			}
		}

		if (cls.hasFields()) {
			for (CodeParameter field : cls.getFields()) {
				writeParameterDeclaration(field);
				writeLine(";");
			}
			writeLine();
		}

		writeSeparated(cls.getMethods(), this::writeMethod, this::writeLine);

		removeTab();
		writeLine("}");
	}

	private void writeMethod(CodeMethod method) {
		writeModifier(method.getModifier());

		if (method.hasReturnType()) {
			writeType(method.getReturnType());
			write(" ");
		}

		write(method.getName(), "(");
		writeSeparated(method.getArguments(), this::writeParameterDeclaration);

		write(")");
		if (method.hasThrows()) {
			write(" throws ");
			writeSeparated(method.getThrows(), this::visit);
		}

		if (method.getModifier().is(CodeModifier.ABSTRACT) || method.getModifier().is(CodeModifier.INTERFACE)) {
			writeLine(";");
		}
		else {
			write(" ");
			method.getBody().visit(this);
		}
	}

	private void writeParameterDeclaration(CodeParameter parameter) {
		writeModifier(parameter.getModifier());
		writeType(parameter.getType());
		write(" ", parameter.getName());
		if (parameter.hasValue()) {
			write(" = ");
			parameter.getValue().visit(this);
		}
	}

	private void writeGenericDeclaration(CodeGeneric generic) {
		write(generic.getName());
		if (generic.hasDependence()) {
			switch (generic.getDependence()) {
				case EXTENDS:
					write(" extends ");
					break;
				case SUPER:
					write(" super ");
					break;
			}
			generic.getDependenceType().visit(this);
		}
	}

	private void writeType(CodeType type) {
		type.visit(this);
	}

	private void writeModifier(CodeModifier modifier) {
		if (modifier.is(CodeModifier.OVERRIDE)) {
			writeLine("@Override");
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
		if (modifier.is(CodeModifier.FINAL)) {
			write("final ");
		}
		if (modifier.is(CodeModifier.ABSTRACT)) {
			write("abstract ");
		}
		if (modifier.is(CodeModifier.STATIC)) {
			write("static ");
		}
	}


	private class LambdaVisitor extends CodeVisitorProxy {
		public LambdaVisitor(CodeVisitor target) {
			super(target);
		}

		@Override
		public void visitStatementExpression(CodeStatementExpression statement) {
			statement.getExpression().visit(JavaCodeFile.this);
		}

		@Override
		public void visitStatementReturn(CodeStatementReturn statement) {
			visitStatementExpression(statement);
		}
	}
}
