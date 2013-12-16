package org.shypl.biser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class Utils
{
	private final static char[] hex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static String representObject(DataObject object)
	{
		StringBuilder builder = new StringBuilder();
		Field[] fields = object.getClass().getFields();

		int i = fields.length;

		builder.append('{');

		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers())) {
				builder.append(field.getName());
				builder.append(": ");
				Class<?> type = field.getType();

				try {
					if (type.isArray()) {
						Class<?> cls = type.getComponentType();
						if (cls.isPrimitive()) {
							if (cls == boolean.class) {
								builder.append(Arrays.toString((boolean[])field.get(object)));
							}
							else if (cls == byte.class) {
								builder.append(Arrays.toString((byte[])field.get(object)));
							}
							else if (cls == short.class) {
								builder.append(Arrays.toString((short[])field.get(object)));
							}
							else if (cls == int.class) {
								builder.append(Arrays.toString((int[])field.get(object)));
							}
							else if (cls == long.class) {
								builder.append(Arrays.toString((long[])field.get(object)));
							}
							else if (cls == double.class) {
								builder.append(Arrays.toString((double[])field.get(object)));
							}
							else {
								throw new ClassCastException("Not supported class " + cls.getName());
							}
						}
						else {
							builder.append(Arrays.toString((Object[])field.get(object)));
						}
					}
					else {
						builder.append(String.valueOf(field.get(object)));
					}
				}
				catch (Exception e) {
					builder.append("<ERROR: ").append(e).append(">");
				}

				if (--i != 0) {
					builder.append(", ");
				}
			}
		}

		builder.append('}');

		return builder.toString();
	}

	public static String convertBytesToHex(byte[] bytes)
	{
		final char[] chars = new char[bytes.length * 2];
		int b;
		int p;
		for (int i = 0; i < bytes.length; i++) {
			b = bytes[i] & 0xFF;
			p = i * 2;
			chars[p] = hex[b >>> 4];
			chars[p + 1] = hex[b & 0x0F];
		}
		return new String(chars);
	}
}
