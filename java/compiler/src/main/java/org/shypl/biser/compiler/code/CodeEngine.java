package org.shypl.biser.compiler.code;

public class CodeEngine extends CodePackage {

	private final CodeNamedObjectSet<CodePrimitive> primitives = new CodeNamedObjectSet<>();
	private final char packagePathSeparator;

	public CodeEngine(char packagePathSeparator) {
		super(null, null);
		this.packagePathSeparator = packagePathSeparator;
	}

	public CodePrimitive getPrimitive(String name) {
		CodePrimitive type = primitives.get(name);
		if (type == null) {
			type = primitives.add(new CodePrimitive(name));
		}
		return type;
	}

	public CodeArray getArray(CodeType elementType) {
		return new CodeArray(elementType);
	}

	public char getPackagePathSeparator() {
		return packagePathSeparator;
	}

	@Override
	public CodeEngine getEngine() {
		return this;
	}
}
