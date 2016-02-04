package org.shypl.biser.compiler.model;

public class MapType extends CollectionType {
	private final DataType keyType;
	private final DataType valueType;

	public MapType(DataType keyType, DataType valueType) {
		super('<' + keyType.getName() + ',' + valueType.getName() + '>');
		this.keyType = keyType;
		this.valueType = valueType;
	}

	@Override
	public <T> T represent(TypeRepresenter<T> representer) {
		return representer.representMap(this);
	}

	public DataType getKeyType() {
		return keyType;
	}

	public DataType getValueType() {
		return valueType;
	}
}
