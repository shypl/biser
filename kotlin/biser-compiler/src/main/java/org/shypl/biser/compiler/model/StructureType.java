package org.shypl.biser.compiler.model;

public abstract class StructureType extends DataType {

	private String packageName;

	public StructureType(String name) {
		super(name);
//		int i = name.lastIndexOf('.');
//		if (i != -1) {
//			setName(name.substring(i + 1));
//			packageName = name.substring(0, i);
//		}
	}

	public boolean hasPackageName() {
		return packageName != null;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getFullName() {
		return hasPackageName() ? packageName + '.' + getName() : getName();
	}
	
	public int getId() {
		throw new UnsupportedOperationException();
	}
}
