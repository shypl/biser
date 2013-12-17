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
	AT,
	AMPERSAND;

	public static Token defineToken(char chr)
	{
		switch (chr) {
			case '{':
				return CURLY_BRACKET_OPEN;
			case '}':
				return CURLY_BRACKET_CLOSE;
			case '(':
				return ROUND_BRACKET_OPEN;
			case ')':
				return ROUND_BRACKET_CLOSE;
			case '<':
				return ANGLE_BRACKET_OPEN;
			case '>':
				return ANGLE_BRACKET_CLOSE;
			case '[':
				return SQUARE_BRACKET_OPEN;
			case ']':
				return SQUARE_BRACKET_CLOSE;
			case ':':
				return COLON;
			case '*':
				return STAR;
			case '^':
				return CARET;
			case '$':
				return DOLLAR;
			case '#':
				return HASH;
			case '-':
				return MINUS;
			case '@':
				return AT;
			case '&':
				return AMPERSAND;
			default:
				return WORD;
		}
	}
}
