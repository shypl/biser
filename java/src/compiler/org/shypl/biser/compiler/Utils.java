package org.shypl.biser.compiler;

public final class Utils {
	public static String convertToSingular(final String string) {
		if (string.endsWith("ies")) {
			return string.substring(0, string.length() - 3).concat("y");
		}

		if (string.endsWith("s") && !string.endsWith("ss") && !string.endsWith("us")) {
			return string.substring(0, string.length() - 1);
		}

		return string;
	}

	public static String convertToCamel(final String string) {
		if (string.isEmpty()) {
			return string;
		}

		char[] buffer = new char[string.length()];
		boolean upNext = true;
		int i = 0;

		for (char c : string.replace('-', '_').toCharArray()) {
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
}
