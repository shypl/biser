package org.shypl.biser.compiler.builder;

public class Mod
{
	public static final int PUBLIC    = 1;
	public static final int PRIVATE   = 2;
	public static final int PROTECTED = 4;
	public static final int STATIC    = 8;
	public static final int FINAL     = 16;
	public static final int ABSTRACT  = 32;
	public static final int INTERFACE = 64;
	public static final int OVERRIDE  = 128;
	public static final int INTERNAL  = 256;
	public static final int GETTER    = 512;
	protected int mod;

	public Mod(final int mod)
	{
		this.mod = mod;
	}

	public String getModString()
	{
		final StringBuilder sb = new StringBuilder();

		if ((mod & PUBLIC) != 0) {
			sb.append("public ");
		}
		if ((mod & PROTECTED) != 0) {
			sb.append("protected ");
		}
		if ((mod & PRIVATE) != 0) {
			sb.append("private ");
		}
		if ((mod & INTERNAL) != 0) {
			sb.append("internal ");
		}
		if ((mod & ABSTRACT) != 0) {
			sb.append("abstract ");
		}
		if ((mod & STATIC) != 0) {
			sb.append("static ");
		}
		if ((mod & FINAL) != 0) {
			sb.append("final ");
		}

		return sb.toString();
	}

	public boolean isAbstract()
	{
		return (mod & ABSTRACT) != 0;
	}

	public boolean isFinal()
	{
		return (mod & FINAL) != 0;
	}

	public boolean isInterface()
	{
		return (mod & INTERFACE) != 0;
	}

	public boolean isOverride()
	{
		return (mod & OVERRIDE) != 0;
	}

	public boolean isPrivate()
	{
		return (mod & PRIVATE) != 0;
	}

	public boolean isProtected()
	{
		return (mod & PROTECTED) != 0;
	}

	public boolean isPublic()
	{
		return (mod & PUBLIC) != 0;
	}

	public boolean isStatic()
	{
		return (mod & STATIC) != 0;
	}

	public boolean isGetter()
	{
		return (mod & GETTER) != 0;
	}
}
