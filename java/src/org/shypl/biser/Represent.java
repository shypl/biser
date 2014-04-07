package org.shypl.biser;

public final class Represent
{
	private final StringBuilder builder = new StringBuilder("{");
	private       boolean       empty   = true;

	Represent()
	{}

	public void add(final String name, final Object value)
	{
		if (empty) {
			empty = false;
		}
		else {
			builder.append(',');
		}
		builder.append(name).append(':').append(value);
	}

	@Override
	public String toString()
	{
		return builder.append('}').toString();
	}
}
