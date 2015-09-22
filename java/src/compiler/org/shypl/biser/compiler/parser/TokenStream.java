package org.shypl.biser.compiler.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class TokenStream {
	static public TokenStream read(BufferedReader reader) throws ParserException {
		final Collection<TokenEntry> entries = new ArrayList<>();
		int line = 0;

		while (true) {
			++line;

			final String string;
			try {
				string = reader.readLine();
			}
			catch (IOException e) {
				throw new ParserException("Error on read line", e);
			}

			if (string == null) {
				break;
			}

			int column = 0;
			final int stringLength = string.length();

			while (column < stringLength) {
				char c = string.charAt(column++);

				if (isWhitespaceChar(c)) {
					continue;
				}

				if (c == '#') {
					break;
				}

				final Token token = defineToken(c);
				if (token == null) {
					throw new ParserException("Unexpected char \"" + c + "\" at " + line + ":" + column);
				}

				if (token == Token.WORD) {
					final int beginIndex = column - 1;
					while (column < stringLength) {
						c = string.charAt(column++);
						if (!isWordChar(c, true)) {
							--column;
							break;
						}
					}
					entries.add(new TokenEntry(token, line, beginIndex + 1, string.substring(beginIndex, column)));
				}
				else {
					entries.add(new TokenEntry(token, line, column, null));
				}
			}
		}

		return new TokenStream(entries);
	}

	private static Token defineToken(final char c) {
		switch (c) {
			case '{':
				return Token.BRACKET_CURLY_OPEN;
			case '}':
				return Token.BRACKET_CURLY_CLOSE;
			case '(':
				return Token.BRACKET_ROUND_OPEN;
			case ')':
				return Token.BRACKET_ROUND_CLOSE;
			case '<':
				return Token.BRACKET_ANGLE_OPEN;
			case '>':
				return Token.BRACKET_ANGLE_CLOSE;
			case '[':
				return Token.BRACKET_SQUARE_OPEN;
			case ']':
				return Token.BRACKET_SQUARE_CLOSE;
			case ':':
				return Token.COLON;
			case '*':
				return Token.STAR;
			case '^':
				return Token.CARET;
			case '-':
				return Token.MINUS;
			case '@':
				return Token.AT;
			case '&':
				return Token.AMPERSAND;
			case ',':
				return Token.COMMA;
			case '.':
				return Token.DOT;
			default:
				if (isWordChar(c, false)) {
					return Token.WORD;
				}
				return null;
		}
	}

	private static boolean isWordChar(final char c, final boolean full) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (full && ((c >= '0' && c <= '9') || c == '_'));
	}

	private static boolean isWhitespaceChar(final char c) {
		return c == ' ' || c == '\t';
	}

	private final TokenEntry[] entries;
	private int position = 0;

	public TokenStream(final Collection<TokenEntry> entries) {
		this.entries = entries.toArray(new TokenEntry[entries.size()]);
	}

	public boolean hasNext() {
		return position != entries.length;
	}

	public TokenEntry next() throws ParserException {
		if (position == entries.length) {
			throw new ParserException("Unexpected end of stream");
		}
		return entries[position++];
	}

	public void back() throws ParserException {
		if (position == 0) {
			throw new ParserException("Beginning of the stream has been reached");
		}
		--position;
	}

	public TokenEntry next(final Token require) throws ParserException {
		final TokenEntry entry = next();
		if (!entry.isToken(require)) {
			throw new UnexpectedTokenException(entry);
		}
		return entry;
	}

	public boolean skip(final Token type) {
		if (hasNext()) {
			if (entries[position].isToken(type)) {
				++position;
				return true;
			}
		}
		return false;
	}
}
