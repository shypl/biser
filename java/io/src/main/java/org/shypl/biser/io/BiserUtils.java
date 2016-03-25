package org.shypl.biser.io;

public class BiserUtils {
	public static byte[] encodeEntity(Entity entity) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		new DataWriter(stream).writeEntity(entity);
		return stream.toArray();
	}

	public static <E extends Entity> E decodeEntity(byte[] bytes, Class<E> type) {
		if (bytes == null) {
			return null;
		}
		return new DataReader(new ByteArrayInputStream(bytes)).readEntity(type);
	}
}
