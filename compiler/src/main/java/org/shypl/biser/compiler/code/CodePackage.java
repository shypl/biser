package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

public class CodePackage extends CodeNamedObject {

	private final CodePackage parent;
	private final CodeNamedObjectSet<CodePackage> packages = new CodeNamedObjectSet<>();
	private final CodeNamedObjectSet<CodeClass>   classes  = new CodeNamedObjectSet<>();

	CodePackage(String name, CodePackage parent) {
		super(name);
		this.parent = parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isFirst() {
		return !isRoot() && parent.isRoot();
	}

	public CodeEngine getEngine() {
		return parent.getEngine();
	}

	public CodePackage getFirstPackage() {
		return isFirst() ? this : parent.getFirstPackage();
	}

	public CodePackage getPackage(String name) {
		int p = name.indexOf('.');

		if (p == -1) {
			CodePackage pack = packages.get(name);
			if (pack == null) {
				pack = packages.add(new CodePackage(name, this));
			}
			return pack;
		}

		return getPackage(name.substring(0, p)).getPackage(name.substring(p + 1));
	}

	public CodeClass getClass(String name) {
		int p = name.lastIndexOf('.');
		if (p != -1) {
			return getPackage(name.substring(0, p)).getClass(name.substring(p + 1));
		}

		CodeClass cls = classes.get(name);
		if (cls == null) {
			cls = classes.add(new CodeClass(name, this));
		}
		return cls;
	}

	public String getFullName(char pathSeparator) {
		Deque<String> parts = new LinkedList<>();
		CodePackage pack = this;
		while (!pack.isRoot()) {
			parts.addFirst(pack.getName());
			pack = pack.parent;
		}

		StringBuilder path = new StringBuilder();
		boolean nf = false;
		for (String name : parts) {
			if (nf) {
				path.append(pathSeparator);
			}
			nf = true;
			path.append(name);
		}

		return path.toString();
	}

	public String getFullName() {
		return getFullName(getEngine().getPackagePathSeparator());
	}

	public Collection<CodeClass> getAllClasses() {
		Collection<CodeClass> collection = new ArrayList<>(classes.getElements());
		for (CodePackage pack : packages) {
			collection.addAll(pack.getAllClasses());
		}
		return collection;
	}
}
