package org.shypl.biser.compiler.prototype;

public class DuplicateNameException extends PrototypeException
{
	private String name;

	public DuplicateNameException(final String message, final String name)
	{
		super(message);
		this.name = name;
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + " (" + name + ")";
	}

	DuplicateNameException expandName(final String name)
	{
		this.name = name + '.' + this.name;
		return this;
	}
}
