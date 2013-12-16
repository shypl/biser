package org.shypl.biser.compiler.code;

public class Mod
{
	public static final int PUBLIC    = 1;
	public static final int PROTECTED = 2;
	public static final int PRIVATE   = 4;
	public static final int INTERNAL  = 8;
	public static final int INTERFACE = 16;
	public static final int OVERRIDE  = 32;
	public static final int STATIC    = 64;
	public static final int ABSTRACT  = 128;
	public static final int FINAL     = 256;
	public static final int GETTER    = 512;

	public static Mod at(int value)
	{
		return new Mod(value);
	}

	///
	private int value;

	private Mod(int value)
	{
		this.value = value;
	}

	public boolean is(int value)
	{
		return (this.value & value) != 0;
	}

	public boolean not(int value)
	{
		return !is(value);
	}

	public void add(int value)
	{
		this.value |= value;
	}
}
