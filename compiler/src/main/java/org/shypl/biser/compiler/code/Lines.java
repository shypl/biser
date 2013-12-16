package org.shypl.biser.compiler.code;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Lines
{
	public final List<String> lines = new LinkedList<>();

	public Lines()
	{
	}

	public Lines(String... lines)
	{
		this.lines.addAll(Arrays.asList(lines));
	}

	public void line(int tab, String... ss)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < tab; i++) {
			builder.append('\t');
		}

		for (String s : ss) {
			builder.append(s);
		}

		lines.add(builder.toString());
	}

	public void line(String... ss)
	{
		StringBuilder builder = new StringBuilder();

		for (String s : ss) {
			builder.append(s);
		}

		lines.add(builder.toString());
	}
}
