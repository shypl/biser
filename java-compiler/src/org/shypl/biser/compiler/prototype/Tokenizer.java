package org.shypl.biser.compiler.prototype;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

public class Tokenizer
{
	private final Entry[] stream;
	private int next = 0;
	private Entry current;

	public Tokenizer(Reader reader) throws IOException
	{
		int line = 1;
		int pos = 0;
		Entry worldEntry = null;
		StringBuilder wordBuilder = null;
		final LinkedList<Entry> list = new LinkedList<>();
		boolean comment = false;

		while (true) {
			int i = reader.read();

			if (i == -1) {
				if (worldEntry != null) {
					worldEntry.word = wordBuilder.toString();
				}
				break;
			}

			char chr = (char)i;

			if (chr == '\n') {
				comment = false;
				++line;
				pos = 0;
				if (worldEntry != null) {
					worldEntry.word = wordBuilder.toString();
					worldEntry = null;
				}
				continue;
			}
			++pos;

			if (comment) {
				continue;
			}

			if (isWhitespace(chr)) {
				if (worldEntry != null) {
					worldEntry.word = wordBuilder.toString();
					worldEntry = null;
				}
				continue;
			}

			Token token = Token.defineToken(chr);

			if (token == Token.WORD) {
				if (worldEntry == null) {
					worldEntry = new Entry(Token.WORD, line, pos);
					wordBuilder = new StringBuilder();
					wordBuilder.append(chr);
					list.add(worldEntry);
				}
				else {
					wordBuilder.append(chr);
				}
				continue;
			}

			if (worldEntry != null) {
				worldEntry.word = wordBuilder.toString();
				worldEntry = null;
			}

			if (token == Token.HASH) {
				comment = true;
				continue;
			}

			list.add(new Entry(token, line, pos));
		}

		stream = list.toArray(new Entry[list.size()]);
	}

	public void check(Token require) throws TokenizerException
	{
		if (current.token != require) {
			throw createUnexpectedTokenException();
		}
	}

	public TokenizerException createUnexpectedTokenException()
	{
		return new TokenizerException(
			"Unexpected token " + current.token + " (" + current.line + ":" + current.pos + ")");
	}

	public Boolean hasNext()
	{
		return next != stream.length;
	}

	public Token next() throws TokenizerException
	{
		if (next == stream.length) {
			throw new TokenizerException("Unexpected end of stream");
		}
		current = stream[next++];
		return current.token;
	}

	public void next(Token require) throws TokenizerException
	{
		next();
		if (current.token != require) {
			throw createUnexpectedTokenException();
		}
	}

	public void prev() throws TokenizerException
	{
		if (next == 0) {
			throw new TokenizerException("Beginning of the stream containsName been reached");
		}

		current = next == 1 ? null : stream[next - 2];
		--next;
	}

	public String word()
	{
		return current.word;
	}

	private boolean isWhitespace(char chr)
	{
		return Character.isWhitespace(chr) || chr == ',';
	}

	private static class Entry
	{
		public final Token  token;
		public final int    line;
		public final int    pos;
		public       String word;

		private Entry(Token token, int line, int pos)
		{
			this.token = token;
			this.line = line;
			this.pos = pos;
		}
	}
}
