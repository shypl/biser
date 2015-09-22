package org.shypl.biser.compiler.parser;

public class TokenEntry {
	private final Token  token;
	private final int    line;
	private final int    column;
	private final String value;

	public TokenEntry(final Token token, final int line, final int column, final String value) {
		this.token = token;
		this.line = line;
		this.column = column;
		this.value = value;
	}

	public Token getToken() {
		return token;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return token.name() + (value != null ? (':' + value) : "") + '(' +  line + ":" + column + ')';
	}

	public boolean isToken(final Token type) {
		return this.token == type;
	}
}
