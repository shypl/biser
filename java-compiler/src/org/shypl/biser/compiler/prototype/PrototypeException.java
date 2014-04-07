package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.CompilerException;

public class PrototypeException extends CompilerException
{
	public PrototypeException(final String message)
	{
		super(message);
	}

	public PrototypeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
