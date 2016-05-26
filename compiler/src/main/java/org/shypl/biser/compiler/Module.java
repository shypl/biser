package org.shypl.biser.compiler;

import org.shypl.biser.compiler.model.ApiSide;

import java.nio.file.Path;
import java.util.Objects;

public class Module {

	private final String  lang;
	private final String  pack;
	private final Path    target;
	private final ApiSide side;

	public Module(String lang, String pack, Path target, ApiSide side) {
		Objects.requireNonNull(lang);
		Objects.requireNonNull(pack);
		Objects.requireNonNull(target);
		Objects.requireNonNull(side);

		this.lang = lang;
		this.pack = pack;
		this.target = target;
		this.side = side;
	}

	public String getLanguage() {
		return lang;
	}

	public String getPackage() {
		return pack;
	}

	public Path getTarget() {
		return target;
	}

	public ApiSide getSide() {
		return side;
	}
}
