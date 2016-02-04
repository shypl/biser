package org.shypl.biser.compiler.parser;

public enum Token {
	BRACKET_CURLY_OPEN, // {
	BRACKET_CURLY_CLOSE, // }
	BRACKET_ROUND_OPEN, // (
	BRACKET_ROUND_CLOSE, // )
	BRACKET_ANGLE_OPEN, // <
	BRACKET_ANGLE_CLOSE, // >
	BRACKET_SQUARE_OPEN, // [
	BRACKET_SQUARE_CLOSE, // ]
	COLON, // :
	STAR, // *
	CARET, // ^
	MINUS, // -
	AT, // @
	AMPERSAND, // &
	COMMA, // ,
	DOT, // .
	WORD // [A-Za-z][A-Za-z0-9_]*
}
