package org.shypl.biser.compiler;

import java.util.Collection;

public class Utils
{
	private static final String[] singularExceptions = new String[]{
		"lives"
	};

	static public String toSingular(String s)
	{
		final String sl = s.toLowerCase();

		for (String exception : singularExceptions) {
			if (exception.equals(sl)) {
				return s;
			}
		}

		if (s.endsWith("ies")) {
			return s.substring(0, s.length() - 3).concat("y");
		}

		if (s.endsWith("s") && !s.endsWith("ss") && !s.endsWith("us")) {
			return s.substring(0, s.length() - 1);
		}

		return s;
	}

	static public String toCamelCase(String s, boolean upFirst)
	{
		if (s.isEmpty()) {
			return s;
		}

		char[] buffer = new char[s.length()];
		boolean upNext = upFirst;
		int i = 0;

		for (char c : s.replace('-', '_').toCharArray()) {
			if (c == '_') {
				upNext = true;
			}
			else if (upNext) {
				buffer[i++] = Character.toUpperCase(c);
				upNext = false;
			}
			else {
				buffer[i++] = c;
			}
		}

		return new String(buffer, 0, i);
	}

	public static String join(final Collection collection, final String sep)
	{
		final StringBuilder string = new StringBuilder();
		boolean first = true;
		for (Object item : collection) {
			if (first) {
				first = false;
			}
			else {
				string.append(sep);
			}
			string.append(item);
		}
		return string.toString();
	}
}
