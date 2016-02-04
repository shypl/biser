package org.shypl.biser.io;

import java.io.IOException;
import java.io.InvalidObjectException;

public abstract class EntityDecoder<T extends Entity> extends TypedDecoder<T> {

	public EntityDecoder(Class<T> type) {
		super(type);
	}

	@Override
	public T decode(BiserReader reader) throws IOException {
		int id = reader.readInt();

		if (id == -1) {
			return null;
		}

		T entity = factory(id);

		if (entity == null) {
			throw new InvalidObjectException("Сan not decode Entity by class " + type.getName() + " (Entity class id " + id + ")");
		}

		entity._decode(reader);

		return entity;
	}

	protected abstract T factory(int id);
}
