package org.shypl.biser.compiler;

import org.shypl.biser.compiler.model.ApiSide;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class Module {

	private final String               name;
	private final String               lang;
	private final String               pack;
	private final Path                 target;
	private final Map<String, ApiSide> apiMap;

	public Module(String name, String lang, String pack, Path target, Map<String, ApiSide> apiMap) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(lang);
		Objects.requireNonNull(target);

		this.name = name;
		this.lang = lang;
		this.pack = pack;
		this.target = target;
		this.apiMap = apiMap;
	}

	public String getName() {
		return name;
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

	public boolean hasApi(String name) {
		return apiMap.containsKey(name);
	}

	public ApiSide getApiSide(String name) {
		return apiMap.get(name);
	}
}
