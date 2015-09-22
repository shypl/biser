package org.shypl.biser.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StreamWriterTest {

	@Test
	public void testWriteByte() throws Exception {
		//arrange
		final byte[] values = {-128, -1, 0, 1, 127};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (byte value : values) {
			writer.writeByte(value);
		}
		for (byte value : values) {
			Encoder.BYTE.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0x80, // -128
			0xFF, // -1
			0x00, // 0
			0x01, // 1
			0x7F, // 127

			0x80, // -128
			0xFF, // -1
			0x00, // 0
			0x01, // 1
			0x7F // 127
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteBool() throws Exception {
		//arrange
		final boolean[] values = {false, true};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (boolean value : values) {
			writer.writeBool(value);
		}
		for (boolean value : values) {
			Encoder.BOOL.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = new byte[]{0x00, 0x01, 0x00, 0x01};
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteInt() throws Exception {
		//arrange
		final int[] values = {-2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (int value : values) {
			writer.writeInt(value);
		}
		for (int value : values) {
			Encoder.INT.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFE, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0x80, // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0x81, // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647

			0xFE, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0x80, // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0x81, // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteUint() throws Exception {
		//arrange
		final int[] values = {-2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (int value : values) {
			writer.writeUint(value);
		}
		for (int value : values) {
			Encoder.UINT.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFE, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0x80, // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0x81, // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647

			0xFE, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0x80, // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0x81, // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteLong() throws Exception {
		//arrange
		final long[] values = {-9223372036854775808L, -2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647, 9223372036854775807L};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (long value : values) {
			writer.writeLong(value);
		}
		for (long value : values) {
			Encoder.LONG.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -9223372036854775808
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x80,  // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81,  // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 9223372036854775807

			0xFE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -9223372036854775808
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x80,  // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81,  // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 9223372036854775807
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteUlong() throws Exception {
		//arrange
		final long[] values = {-9223372036854775808L, -2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647, 9223372036854775807L};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (long value : values) {
			writer.writeUlong(value);
		}
		for (long value : values) {
			Encoder.ULONG.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -9223372036854775808
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x80,  // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81,  // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 9223372036854775807

			0xFE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -9223372036854775808
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0x80, 0x00, 0x00, 0x00, // -2147483648
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x80,  // -128
			0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81,  // -127
			0xFF, // -1
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFD, // 253
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE, // 254
			0xFE, 0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
			0xFE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 9223372036854775807
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteDouble() throws Exception {
		//arrange
		final double[] values = {
			0, 1, -1, 1.5, -1.5,
			Double.MIN_NORMAL, Double.MIN_VALUE, Double.MAX_VALUE,
			Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
		};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (double value : values) {
			writer.writeDouble(value);
		}
		for (double value : values) {
			Encoder.DOUBLE.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
			0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1
			0xBF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1
			0x3F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1.5
			0xBF, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1.5
			0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // MIN_NORMAL 2.2250738585072014E-308
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, // MIN_VALUE  4.9e-324
			0x7F, 0xEF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // MAX_VALUE  1.7976931348623157e+308
			0x7F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NaN
			0xFF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NEGATIVE_INFINITY
			0x7F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // POSITIVE_INFINITY

			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
			0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1
			0xBF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1
			0x3F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1.5
			0xBF, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1.5
			0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // MIN_NORMAL 2.2250738585072014E-308
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, // MIN_VALUE  4.9e-324
			0x7F, 0xEF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // MAX_VALUE  1.7976931348623157e+308
			0x7F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NaN
			0xFF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NEGATIVE_INFINITY
			0x7F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // POSITIVE_INFINITY
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteBytes() throws Exception {
		//arrange
		final byte[][] values = {
			null,
			{},
			{-128, -1, 0, 1, 127}
		};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (byte[] value : values) {
			writer.writeBytes(value);
		}
		for (byte[] value : values) {
			Encoder.BYTES.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF, // null
			0x00, // {}
			0x05, 0x80, 0xFF, 0x00, 0x01, 0x7F, // {-128, -1, 0, 1, 127}

			0xFF, // null
			0x00, // {}
			0x05, 0x80, 0xFF, 0x00, 0x01, 0x7F // {-128, -1, 0, 1, 127}
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteString() throws Exception {
		//arrange
		final String[] values = {
			null,
			"",
			"Hello World!",
			"\n \t \r 1234567890±!@#$%^&*()_+<>,./?{}[];\'\\:\"|",
			"A pile of poo: \uD83D\uDCA9.",
			"你好世界！",
			"مرحبا العالم!"
		};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (String value : values) {
			writer.writeString(value);
		}
		for (String value : values) {
			Encoder.STRING.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x0C, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21,
			0x2E, 0x0A, 0x20, 0x09, 0x20, 0x0D, 0x20, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0xC2, 0xB1, 0x21, 0x40, 0x23, 0x24, 0x25, 0x5E, 0x26, 0x2A, 0x28, 0x29, 0x5F, 0x2B, 0x3C, 0x3E, 0x2C, 0x2E, 0x2F, 0x3F, 0x7B, 0x7D, 0x5B, 0x5D, 0x3B, 0x27, 0x5C, 0x3A, 0x22, 0x7C,
			0x14, 0x41, 0x20, 0x70, 0x69, 0x6C, 0x65, 0x20, 0x6F, 0x66, 0x20, 0x70, 0x6F, 0x6F, 0x3A, 0x20, 0xF0, 0x9F, 0x92, 0xA9, 0x2E,
			0x0F, 0xE4, 0xBD, 0xA0, 0xE5, 0xA5, 0xBD, 0xE4, 0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81,
			0x18, 0xD9, 0x85, 0xD8, 0xB1, 0xD8, 0xAD, 0xD8, 0xA8, 0xD8, 0xA7, 0x20, 0xD8, 0xA7, 0xD9, 0x84, 0xD8, 0xB9, 0xD8, 0xA7, 0xD9, 0x84, 0xD9, 0x85, 0x21,

			0xFF,
			0x00,
			0x0C, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21,
			0x2E, 0x0A, 0x20, 0x09, 0x20, 0x0D, 0x20, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0xC2, 0xB1, 0x21, 0x40, 0x23, 0x24, 0x25, 0x5E, 0x26, 0x2A, 0x28, 0x29, 0x5F, 0x2B, 0x3C, 0x3E, 0x2C, 0x2E, 0x2F, 0x3F, 0x7B, 0x7D, 0x5B, 0x5D, 0x3B, 0x27, 0x5C, 0x3A, 0x22, 0x7C,
			0x14, 0x41, 0x20, 0x70, 0x69, 0x6C, 0x65, 0x20, 0x6F, 0x66, 0x20, 0x70, 0x6F, 0x6F, 0x3A, 0x20, 0xF0, 0x9F, 0x92, 0xA9, 0x2E,
			0x0F, 0xE4, 0xBD, 0xA0, 0xE5, 0xA5, 0xBD, 0xE4, 0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81,
			0x18, 0xD9, 0x85, 0xD8, 0xB1, 0xD8, 0xAD, 0xD8, 0xA8, 0xD8, 0xA7, 0x20, 0xD8, 0xA7, 0xD9, 0x84, 0xD8, 0xB9, 0xD8, 0xA7, 0xD9, 0x84, 0xD9, 0x85, 0x21,
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteDate() throws Exception {
		//arrange
		final Date[] values = {null, new Date(0), new Date(-100000), new Date(100000), new Date(Integer.MAX_VALUE)};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (Date value : values) {
			writer.writeDate(value);
		}
		for (Date value : values) {
			Encoder.DATE.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // null
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0x79, 0x60, // -100000
			0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x86, 0xA0, // 100000
			0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // Integer.MAX_VALUE

			0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // null
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0x79, 0x60, // -100000
			0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x86, 0xA0, // 100000
			0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // Integer.MAX_VALUE
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteEnum() throws Exception {
		//arrange
		final Enum<?>[] values = {null, EnumStub.VALUE};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (Enum<?> value : values) {
			writer.writeEnum(value);
		}
		for (Enum<?> value : values) {
			Encoder.ENUM.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{0xFF, 0x00, 0xFF, 0x00});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteEntity() throws Exception {
		//arrange
		final Entity[] values = {null, new EntityStub()};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (Entity value : values) {
			writer.writeEntity(value);
		}
		for (Entity value : values) {
			Encoder.ENTITY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{0xFF, 0x00, 0xFF, 0x00});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteByteArray() throws Exception {
		//arrange
		final byte[][] values = {null, {}, {1}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (byte[] value : values) {
			writer.writeByteArray(value);
		}
		for (byte[] value : values) {
			Encoder.BYTE_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01,
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteBoolArray() throws Exception {
		//arrange
		final boolean[][] values = {null, {}, {true}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (boolean[] value : values) {
			writer.writeBoolArray(value);
		}
		for (boolean[] value : values) {
			Encoder.BOOL_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01,
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteIntArray() throws Exception {
		//arrange
		final int[][] values = {null, {}, {1}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (int[] value : values) {
			writer.writeIntArray(value);
		}
		for (int[] value : values) {
			Encoder.INT_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01,
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteUintArray() throws Exception {
		//arrange
		final int[][] values = {null, {}, {1}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (int[] value : values) {
			writer.writeUintArray(value);
		}
		for (int[] value : values) {
			Encoder.UINT_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01,
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteLongArray() throws Exception {
		//arrange
		final long[][] values = {null, {}, {1}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (long[] value : values) {
			writer.writeLongArray(value);
		}
		for (long[] value : values) {
			Encoder.LONG_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01,
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteUlongArray() throws Exception {
		//arrange
		final long[][] values = {null, {}, {1}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (long[] value : values) {
			writer.writeUlongArray(value);
		}
		for (long[] value : values) {
			Encoder.ULONG_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteDoubleArray() throws Exception {
		//arrange
		final double[][] values = {null, {}, {1}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (double[] value : values) {
			writer.writeDoubleArray(value);
		}
		for (double[] value : values) {
			Encoder.DOUBLE_ARRAY.encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

			0xFF,
			0x00,
			0x01, 0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteArray() throws Exception {
		//arrange
		final String[][] values = {null, {}, {"A", "B"}};

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (String[] value : values) {
			writer.writeArray(value, Encoder.STRING);
		}
		for (String[] value : values) {
			Encoder.forArray(Encoder.STRING).encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x02, 0x01, 0x41, 0x01, 0x42,

			0xFF,
			0x00,
			0x02, 0x01, 0x41, 0x01, 0x42
		});
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testWriteMap() throws Exception {
		//arrange
		@SuppressWarnings("unchecked")
		final Map<String, String>[] values = new Map[3];
		values[0] = null;
		values[1] = new HashMap<>();
		values[2] = new HashMap<>();
		values[2].put("A", "B");

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final StreamWriter writer = new StreamWriter(stream);

		//act
		for (Map<String, String> value : values) {
			writer.writeMap(value, Encoder.STRING, Encoder.STRING);
		}
		for (Map<String, String> value : values) {
			Encoder.forMap(Encoder.STRING, Encoder.STRING).encode(value, writer);
		}

		//assert
		final byte[] actual = stream.toByteArray();
		final byte[] expected = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01, 0x41, 0x01, 0x42,

			0xFF,
			0x00,
			0x01, 0x01, 0x41, 0x01, 0x42
		});
		Assert.assertArrayEquals(expected, actual);
	}
}