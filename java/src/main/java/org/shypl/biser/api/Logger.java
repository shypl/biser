package org.shypl.biser.api;

public interface Logger
{
	public boolean isDebug();

	public boolean isError();

	public void debug(final String message, Object... args);

	public void error(final String message, Object... args);
}
