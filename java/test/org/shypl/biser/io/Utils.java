package org.shypl.biser.io;

public class Utils {
	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

	public static void printBytes(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 6];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 6] = '0';
			hexChars[j * 6 + 1] = 'x';
			hexChars[j * 6 + 2] = HEX_CHARS[v >>> 4];
			hexChars[j * 6 + 3] = HEX_CHARS[v & 0x0F];
			hexChars[j * 6 + 4] = ',';
			hexChars[j * 6 + 5] = ' ';
		}
		System.out.println(new String(hexChars));
	}

	public static byte[] convertIntArrayToByteArray(int[] ints) {
		final byte[] bytes = new byte[ints.length];
		for (int i = 0; i < ints.length; i++) {
			bytes[i] = (byte)ints[i];
		}
		return bytes;
	}
}
