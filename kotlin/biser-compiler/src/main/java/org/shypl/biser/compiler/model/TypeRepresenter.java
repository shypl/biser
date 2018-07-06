package org.shypl.biser.compiler.model;

public interface TypeRepresenter<T> {
	default T representPrimitive(PrimitiveType type) {
		throw new UnsupportedOperationException();
	}
	
	default T representEntity(EntityType type) {
		throw new UnsupportedOperationException();
	}
	
	default T representEnum(EnumType type) {
		throw new UnsupportedOperationException();
	}
	
	default T representArray(ArrayType type) {
		throw new UnsupportedOperationException();
	}
	
	default T representMap(MapType type) {
		throw new UnsupportedOperationException();
	}
}
