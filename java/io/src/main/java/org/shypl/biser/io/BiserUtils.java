package org.shypl.biser.io;

public class BiserUtils {
	public static byte[] writeEntity(Entity entity) {
		ByteArrayWriter writer = new ByteArrayWriter();
		writer.writeEntity(entity);
		return writer.toByteArray();
	}

	public static <E extends Entity> E readEntity(byte[] bytes, Class<E> type) {
		if (bytes == null) {
			return null;
		}
		return new ByteArrayReader(bytes).readEntity(type);
	}
}
