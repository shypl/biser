package org.shypl.biser.compiler.parser;

public class UnexpectedTokenException extends ParserException {
	public UnexpectedTokenException(final TokenEntry entry) {
		super("Unexpected token " + entry.toString());
	}
}
