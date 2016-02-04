package org.shypl.biser.compiler.parser;

import org.shypl.biser.compiler.CompilerException;

public class ParserException extends CompilerException {
	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}
}
