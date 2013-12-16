package org.shypl.biser.compiler.code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class FileBuilder
{
	private final StringBuilder builder;

	public FileBuilder()
	{
		builder = new StringBuilder();
	}

	public void save(File file) throws IOException
	{
		File dir = file.getParentFile();

		if (!dir.exists()) {
			//noinspection ResultOfMethodCallIgnored
			dir.mkdirs();
		}

		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		writer.write(builder.toString());
		writer.close();
	}

	public void add(int tab, String... ss)
	{
		tab(tab);
		for (String s : ss) {
			builder.append(s);
		}
	}

	public void add(String... ss)
	{
		add(0, ss);
	}

	public void line(int tab, String... ss)
	{
		tab(tab);

		for (String s : ss) {
			builder.append(s);
		}

		builder.append('\n');
	}

	public void line(String... ss)
	{
		line(0, ss);
	}

	public void lines(int tab, String... ss)
	{
		for (String s : ss) {
			tab(tab);
			builder.append(s);
			builder.append('\n');
		}
	}

	public void lines(String... ss)
	{
		lines(0, ss);
	}

	private void tab(int tab)
	{
		for (int i = 0; i < tab; i++) {
			builder.append('\t');
		}
	}
}
