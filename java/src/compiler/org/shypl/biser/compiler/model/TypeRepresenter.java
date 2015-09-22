package org.shypl.biser.compiler.model;

public interface TypeRepresenter<T> {
	T representPrimitive(PrimitiveType type);

	T representEntity(EntityType type);

	T representEnum(EnumType type);

	T representArray(ArrayType type);

	T representMap(MapType type);
}
