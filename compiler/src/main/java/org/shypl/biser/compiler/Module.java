package org.shypl.biser.compiler;

import org.shypl.biser.compiler.model.CsiSide;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class Module {

	private final String               name;
	private final String               lang;
	private final String               pack;
	private final Path                 target;
	private final Map<String, CsiSide> csi;

	public Module(String name, String lang, String pack, Path target, Map<String, CsiSide> csi) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(lang);
		Objects.requireNonNull(target);

		this.name = name;
		this.lang = lang;
		this.pack = pack;
		this.target = target;
		this.csi = csi;
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

	public boolean hasCsi(String name) {
		return csi.containsKey(name);
	}

	public CsiSide getCsiSide(String name) {
		return csi.get(name);
	}

	public Map<String, CsiSide> getCsi() {
		return csi;
	}
}
