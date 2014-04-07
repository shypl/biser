package org.shypl.biser;

public abstract class BiserEntity
{
	public void encode(final OutputBuffer buffer)
	{
	}

	@Override
	public final String toString()
	{
		final Represent r = new Represent();
		represent(r);
		return r.toString();
	}

	protected int _eid()
	{
		return 0;
	}

	protected void represent(final Represent r)
	{}
}
