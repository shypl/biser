package org.shypl.biser.compiler;

public enum Token
{
	WORD,
	CURLY_BRACKET_OPEN,
	CURLY_BRACKET_CLOSE,
	ROUND_BRACKET_OPEN,
	ROUND_BRACKET_CLOSE,
	ANGLE_BRACKET_OPEN,
	ANGLE_BRACKET_CLOSE,
	SQUARE_BRACKET_OPEN,
	SQUARE_BRACKET_CLOSE,
	COLON,
	STAR,
	CARET,
	DOLLAR,
	HASH,
	MINUS,
	AT;

	public static Token defineToken(char chr)
	{
		switch (chr) {
			case '{':
				return Token.CURLY_BRACKET_OPEN;
			case '}':
				return Token.CURLY_BRACKET_CLOSE;
			case '(':
				return Token.ROUND_BRACKET_OPEN;
			case ')':
				return Token.ROUND_BRACKET_CLOSE;
			case '<':
				return Token.ANGLE_BRACKET_OPEN;
			case '>':
				return Token.ANGLE_BRACKET_CLOSE;
			case '[':
				return Token.SQUARE_BRACKET_OPEN;
			case ']':
				return Token.SQUARE_BRACKET_CLOSE;
			case ':':
				return Token.COLON;
			case '*':
				return Token.STAR;
			case '^':
				return Token.CARET;
			case '$':
				return Token.DOLLAR;
			case '#':
				return Token.HASH;
			case '-':
				return Token.MINUS;
			case '@':
				return Token.AT;
			default:
				return Token.WORD;
		}
	}
}
