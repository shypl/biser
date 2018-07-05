package org.shypl.biser.compiler.code;

public class CodeExpressionWord implements CodeExpression {

	public static final CodeExpressionWord THIS  = new CodeExpressionWord("this");
	public static final CodeExpressionWord SUPER = new CodeExpressionWord("super");
	public static final CodeExpressionWord NULL  = new CodeExpressionWord("null");
	public static final CodeExpressionWord _0    = new CodeExpressionWord("0");
	public static final CodeExpression     TRUE  = new CodeExpressionWord("true");
	public static final CodeExpression     FALSE = new CodeExpressionWord("false");

	private final String word;

	public CodeExpressionWord(String word) {
		this.word = word;
	}

	public CodeExpressionWord(int word) {
		this(String.valueOf(word));
	}

	public String getWord() {
		return word;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitExpressionWord(this);
	}
}
