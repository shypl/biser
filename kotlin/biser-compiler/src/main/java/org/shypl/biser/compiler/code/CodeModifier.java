package org.shypl.biser.compiler.code;

public class CodeModifier implements Comparable<CodeModifier> {
	public static final int NONE      = 0;
	public static final int STATIC    = 1;
	public static final int PUBLIC    = 2;
	public static final int PROTECTED = 4;
	public static final int PRIVATE   = 8;
	public static final int INTERNAL  = 16;
	public static final int FINAL     = 32;
	public static final int OVERRIDE  = 64;
	public static final int ABSTRACT  = 128;
	public static final int GETTER    = 256;
	public static final int SETTER    = 512;
	public static final int INTERFACE = 1024;

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

	public int weight() {
		int w = 0;

		if (is(STATIC)) {
			w += 0x100;
		}

		if (is(PUBLIC)) {
			w += 0x080;
		}
		if (is(PROTECTED)) {
			w += 0x040;
		}
		if (is(PRIVATE)) {
			w += 0x020;
		}
		if (is(INTERNAL)) {
			w += 0x010;
		}

		if (is(FINAL)) {
			w += 0x008;
		}
		if (is(OVERRIDE)) {
			w += 0x004;
		}
		if (is(ABSTRACT)) {
			w += 0x002;
		}
		if (is(GETTER) || is(SETTER)) {
			w += 0x001;
		}

		return w;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CodeModifier && ((CodeModifier)obj).value == value;
	}

	@Override
	public int compareTo(CodeModifier o) {
		return Integer.compare(o.weight(), weight());
	}
}
