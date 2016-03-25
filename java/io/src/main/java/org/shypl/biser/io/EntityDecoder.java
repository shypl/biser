package org.shypl.biser.io;

public abstract class EntityDecoder<T extends Entity> extends TypedDecoder<T> {

	public EntityDecoder(Class<T> type) {
		super(type);
	}

	@Override
	public T decode(DataReader reader) {
		int id = reader.readInt();

		if (id == -1) {
			return null;
		}

		T entity = factory(id);

		if (entity == null) {
			throw new RuntimeException("Сan not decode Entity by class " + type.getName() + " (Entity class id " + id + ")");
		}

		entity._decode(reader);

		return entity;
	}

	protected abstract T factory(int id);
}
