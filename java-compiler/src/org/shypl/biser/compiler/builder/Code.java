package org.shypl.biser.compiler.builder;

public class Code
{
	private final StringBuilder stream = new StringBuilder();

	public Code add(final Object... args)
	{
		for (Object arg : args) {
			stream.append(arg);
		}
		return this;
	}

	public Code addTab(int indent, final Object... args)
	{
		tab(indent);
		return add(args);
	}

	public Code tab(int indent)
	{
		while (--indent >= 0) {
			stream.append('\t');
		}
		return this;
	}

	public Code lineTab(int indent, final Object... args)
	{
		tab(indent);
		return line(args);
	}

	public Code line(final Object... args)
	{
		add(args);
		stream.append('\n');
		return this;
	}

	@Override
	public String toString()
	{
		return stream.toString();
	}

	public String toString(int indent)
	{
		if (indent == 0) {
			return toString();
		}

		String indentStr = "";
		while (--indent >= 0) {
			indentStr += '\t';
		}
		String string = indentStr + toString().replace("\n", '\n' + indentStr);

		return string.substring(0, string.length() - indentStr.length());
	}
}
