package org.shypl.biser.compiler.prototype;

public enum Token
{
	BRACKET_CURLY_OPEN('{'),
	BRACKET_CURLY_CLOSE('}'),
	BRACKET_ROUND_OPEN('('),
	BRACKET_ROUND_CLOSE(')'),
	BRACKET_ANGLE_OPEN('<'),
	BRACKET_ANGLE_CLOSE('>'),
	BRACKET_SQUARE_OPEN('['),
	BRACKET_SQUARE_CLOSE(']'),
	COLON(':'),
	STAR('*'),
	CARET('^'),
	DOLLAR('$'),
	HASH('#'),
	MINUS('-'),
	AT('@'),
	AMPERSAND('&'),
	WORD('\0');

	public static Token defineToken(char chr)
	{
		for (Token token : values()) {
			if (token.chr == chr) {
				return token;
			}
		}

		return WORD;
	}

	private final char chr;

	Token(final char chr)
	{
		this.chr = chr;
	}
}
