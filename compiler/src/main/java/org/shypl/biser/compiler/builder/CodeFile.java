package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.CompilerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

public class CodeFile {
	private final StringBuilder content = new StringBuilder();
	private int tabs;
	private int emptyLines;

	public void save(final Path path) throws CompilerException {
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, content.toString().getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			throw new CompilerException("Can not save file \"" + path + "\"", e);
		}
	}

	public CodeFile write(final String... strings) {
		if (emptyLines != 0) {
			for (int l = 0; l < emptyLines; ++l) {
				content.append('\n');
				for (int t = 0; t < tabs; ++t) {
					content.append('\t');
				}
			}
			emptyLines = 0;
		}
		for (String string : strings) {
			content.append(string);
		}
		return this;
	}

	public CodeFile writeLine(final String... strings) {
		if (strings.length > 0) {
			write(strings);
		}
		if (emptyLines < 2) {
			++emptyLines;
		}
		return this;
	}

	public <T> void writeSeparated(Collection<T> collection, Consumer<T> action) {
		writeSeparated(collection, action, ", ");
	}

	public <T> void writeSeparated(Collection<T> collection, Consumer<T> action, String separator) {
		boolean nf = false;
		for (T e : collection) {
			if (nf) {
				write(separator);
			}
			nf = true;
			action.accept(e);
		}
	}

	public <T> void writeSeparated(Collection<T> collection, Consumer<T> action, Runnable separator) {
		boolean nf = false;
		for (T e : collection) {
			if (nf) {
				separator.run();
			}
			nf = true;
			action.accept(e);
		}
	}

	public CodeFile addTab() {
		++tabs;
		return this;
	}

	public CodeFile removeTab() {
		if (emptyLines > 1) {
			emptyLines = 1;
		}
		--tabs;
		return this;
	}
}
