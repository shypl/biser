package org.shypl.biser.compiler.code;

public class CodeModifier {
	public static final int NONE      = 0x000;
	public static final int PUBLIC    = 0x001;
	public static final int PROTECTED = 0x002;
	public static final int PRIVATE   = 0x004;
	public static final int STATIC    = 0x008;
	public static final int FINAL     = 0x010;
	public static final int ABSTRACT  = 0x020;
	public static final int OVERRIDE  = 0x040;
	public static final int INTERNAL  = 0x080;
	public static final int GETTER    = 0x100;
	public static final int SETTER    = 0x200;
	public static final int CONST     = 0x400;
	public static final int INTERFACE = 0x800;

	private int value = NONE;

	public CodeModifier() {
		this(0);
	}

	public CodeModifier(int value) {
		this.value = value;
	}

	public void clear() {
		this.value = 0;
	}

	public void set(int value) {
		this.value = value;
	}

	public void add(int value) {
		this.value |= value;
	}

	public void remove(int value) {
		if (is(value)) {
			this.value = this.value & ~value;
		}
	}

	public boolean is(int value) {
		return (this.value & value) == value;
	}

	public boolean not(int value) {
		return !is(value);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CodeModifier && ((CodeModifier)obj).value == value;
	}
}
