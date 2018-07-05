package org.shypl.biser.compiler.model;

public class ArrayType extends CollectionType {

	private final DataType elementType;

	public ArrayType(DataType elementType) {
		super('*' + elementType.getName());
		this.elementType = elementType;
	}

	@Override
	public <T> T represent(TypeRepresenter<T> representer) {
		return representer.representArray(this);
	}

	public DataType getElementType() {
		return elementType;
	}
}