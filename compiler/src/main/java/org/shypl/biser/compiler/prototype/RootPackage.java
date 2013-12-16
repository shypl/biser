package org.shypl.biser.compiler.prototype;

public class RootPackage extends Package
{
	private String target;

	public RootPackage()
	{
		super(null, null);
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String fullName()
	{
		return target;
	}
}
