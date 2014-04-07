package org.shypl.biser.server;

public class BadRequestFormatException extends RuntimeException
{
	public BadRequestFormatException()
	{}

	public BadRequestFormatException(final String message)
	{
		super(message);
	}

	public BadRequestFormatException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
