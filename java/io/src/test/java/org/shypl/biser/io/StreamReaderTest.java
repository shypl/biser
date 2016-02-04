package org.shypl.biser.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StreamReaderTest {

	@Test
	public void testReadByte() throws Exception {
		//arrange
		final byte[] expectedValues = {-128, -1, 0, 1, 127};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
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

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (byte expected : expectedValues) {
			//act
			final byte actual = reader.readByte();

			//assert
			Assert.assertEquals(expected, actual);
		}
		for (byte expected : expectedValues) {
			//act
			final byte actual = Decoder.BYTE.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadBool() throws Exception {
		//arrange
		final boolean[] expectedValues = {false, true};
		final byte[] data = {0x00, 0x01, 0x00, 0x01};

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (boolean expected : expectedValues) {
			//act
			final boolean actual = reader.readBool();

			//assert
			Assert.assertEquals(expected, actual);
		}

		for (boolean expected : expectedValues) {
			//act
			final boolean actual = Decoder.BOOL.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadInt() throws Exception {
		//arrange
		final int[] expectedValues = {-2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
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

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (int expected : expectedValues) {
			//act
			final int actual = reader.readInt();

			//assert
			Assert.assertEquals(expected, actual);
		}

		for (int expected : expectedValues) {
			//act
			final int actual = Decoder.INT.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}


		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadUint() throws Exception {
		//arrange
		final long[] expectedValues = {0, 127, 128, 254, 255, 2147483647, 4294967295L};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFE, // 254
			0xFF, 0x00, 0x00, 0x00, 0xFF, // 255
			0xFF, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 4294967295L

			0x00, // 0
			0x7F, // 127
			0x80, // 128
			0xFE, // 254
			0xFF, 0x00, 0x00, 0x00, 0xFF, // 255
			0xFF, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 4294967295L
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (long expected : expectedValues) {
			//act
			final long actual = reader.readUint();

			//assert
			Assert.assertEquals(expected, actual);
		}

		for (long expected : expectedValues) {
			//act
			final long actual = Decoder.UINT.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadLong() throws Exception {
		//arrange
		final long[] expectedValues = {-9223372036854775808L, -2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647, 9223372036854775807L};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
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

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (long expected : expectedValues) {
			//act
			final long actual = reader.readLong();

			//assert
			Assert.assertEquals(expected, actual);
		}

		for (long expected : expectedValues) {
			//act
			final long actual = Decoder.LONG.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadUlong() throws Exception {
		//arrange
		final long[] expectedValues = {-9223372036854775808L, -2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647, 9223372036854775807L};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
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

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (long expected : expectedValues) {
			//act
			final long actual = reader.readUlong();

			//assert
			Assert.assertEquals(expected, actual);
		}
		for (long expected : expectedValues) {
			//act
			final long actual = Decoder.ULONG.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadDouble() throws Exception {
		//arrange
		final double[] expectedValues = {
			0, 1, -1, 1.5, -1.5,
			Double.MIN_NORMAL, Double.MIN_VALUE, Double.MAX_VALUE,
			Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
		};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
			0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1
			0xBF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1
			0x3F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1.5
			0xBF, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1.5
			0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // MIN_NORMAL 2.2250738585072014E-308
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, // MIN_expected  4.9e-324
			0x7F, 0xEF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // MAX_expected  1.7976931348623157e+308
			0x7F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NaN
			0xFF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NEGATIVE_INFINITY
			0x7F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // POSITIVE_INFINITY

			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
			0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1
			0xBF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1
			0x3F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 1.5
			0xBF, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -1.5
			0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // MIN_NORMAL 2.2250738585072014E-308
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, // MIN_expected  4.9e-324
			0x7F, 0xEF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // MAX_expected  1.7976931348623157e+308
			0x7F, 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NaN
			0xFF, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // NEGATIVE_INFINITY
			0x7F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // POSITIVE_INFINITY
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (double expected : expectedValues) {
			//act
			final double actual = reader.readDouble();

			//assert
			Assert.assertEquals(expected, actual, 0);
		}

		for (double expected : expectedValues) {
			//act
			final double actual = Decoder.DOUBLE.decode(reader);

			//assert
			Assert.assertEquals(expected, actual, 0);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadBytes() throws Exception {
		//arrange
		final byte[][] expectedValues = {
			null,
			{},
			{-128, -1, 0, 1, 127}
		};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF, // null
			0x00, // {}
			0x05, 0x80, 0xFF, 0x00, 0x01, 0x7F, // {-128, -1, 0, 1, 127}

			0xFF, // null
			0x00, // {}
			0x05, 0x80, 0xFF, 0x00, 0x01, 0x7F, // {-128, -1, 0, 1, 127}
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (byte[] expected : expectedValues) {
			//act
			final byte[] actual = reader.readBytes();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (byte[] expected : expectedValues) {
			//act
			final byte[] actual = Decoder.BYTES.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}


		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadString() throws Exception {
		//arrange
		final String[] expectedValues = {
			null,
			"",
			"Hello World!",
			"\n \t \r 1234567890±!@#$%^&*()_+<>,./?{}[];\'\\:\"|",
			"A pile of poo: \uD83D\uDCA9.",
			"你好世界！",
			"مرحبا العالم!"
		};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
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
			0x18, 0xD9, 0x85, 0xD8, 0xB1, 0xD8, 0xAD, 0xD8, 0xA8, 0xD8, 0xA7, 0x20, 0xD8, 0xA7, 0xD9, 0x84, 0xD8, 0xB9, 0xD8, 0xA7, 0xD9, 0x84, 0xD9, 0x85, 0x21
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (String expected : expectedValues) {
			//act
			final String actual = reader.readString();

			//assert
			Assert.assertEquals(expected, actual);
		}
		for (String expected : expectedValues) {
			//act
			final String actual = Decoder.STRING.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}


		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadDate() throws Exception {
		//arrange
		final Date[] expectedValues = {null, new Date(0), new Date(-100000), new Date(100000), new Date(Integer.MAX_VALUE)};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
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

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (Date expected : expectedValues) {
			//act
			final Date actual = reader.readDate();

			//assert
			Assert.assertEquals(expected, actual);
		}
		for (Date expected : expectedValues) {
			//act
			final Date actual = Decoder.DATE.decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadEnum() throws Exception {
		//arrange
		final Enum<?>[] expectedValues = {null, EnumStub.VALUE};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{0xFF, 0x00});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (Enum<?> expected : expectedValues) {
			//act
			final Enum<?> actual = reader.readEnum(EnumStub.class);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadEntity() throws Exception {
		//arrange
		final Entity[] expectedValues = {null, new EntityStub()};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{0xFF, 0x00});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (Entity expected : expectedValues) {
			//act
			final EntityStub actual = reader.readEntity(EntityStub.class);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadByteArray() throws Exception {
		//arrange
		final byte[][] expectedValues = {null, {}, {1}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01,
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (byte[] expected : expectedValues) {
			//act
			final byte[] actual = reader.readByteArray();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (byte[] expected : expectedValues) {
			//act
			final byte[] actual = Decoder.BYTE_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadBoolArray() throws Exception {
		//arrange
		final boolean[][] expectedValues = {null, {}, {true}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (boolean[] expected : expectedValues) {
			//act
			final boolean[] actual = reader.readBoolArray();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (boolean[] expected : expectedValues) {
			//act
			final boolean[] actual = Decoder.BOOL_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadIntArray() throws Exception {
		//arrange
		final int[][] expectedValues = {null, {}, {1}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (int[] expected : expectedValues) {
			//act
			final int[] actual = reader.readIntArray();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (int[] expected : expectedValues) {
			//act
			final int[] actual = Decoder.INT_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadUintArray() throws Exception {
		//arrange
		final int[][] expectedValues = {null, {}, {1}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (int[] expected : expectedValues) {
			//act
			final int[] actual = reader.readUintArray();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (int[] expected : expectedValues) {
			//act
			final int[] actual = Decoder.UINT_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadLongArray() throws Exception {
		//arrange
		final long[][] expectedValues = {null, {}, {1}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (long[] expected : expectedValues) {
			//act
			final long[] actual = reader.readLongArray();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (long[] expected : expectedValues) {
			//act
			final long[] actual = Decoder.LONG_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadUlongArray() throws Exception {
		//arrange
		final long[][] expectedValues = {null, {}, {1}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01,

			0xFF,
			0x00,
			0x01, 0x01
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (long[] expected : expectedValues) {
			//act
			final long[] actual = reader.readUlongArray();

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (long[] expected : expectedValues) {
			//act
			final long[] actual = Decoder.ULONG_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadDoubleArray() throws Exception {
		//arrange
		final double[][] expectedValues = {null, {}, {1}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

			0xFF,
			0x00,
			0x01, 0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (double[] expected : expectedValues) {
			//act
			final double[] actual = reader.readDoubleArray();

			//assert
			Assert.assertArrayEquals(expected, actual, 0);
		}
		for (double[] expected : expectedValues) {
			//act
			final double[] actual = Decoder.DOUBLE_ARRAY.decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual, 0);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadArray() throws Exception {
		//arrange
		final String[][] expectedValues = {null, {}, {"A", "B"}};
		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x02, 0x01, 0x41, 0x01, 0x42,

			0xFF,
			0x00,
			0x02, 0x01, 0x41, 0x01, 0x42,
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (String[] expected : expectedValues) {
			//act
			final String[] actual = reader.readArray(Decoder.STRING);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}
		for (String[] expected : expectedValues) {
			//act
			final String[] actual = Decoder.forArray(Decoder.STRING).decode(reader);

			//assert
			Assert.assertArrayEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadMap() throws Exception {
		//arrange
		@SuppressWarnings("unchecked")
		final Map<String, String>[] expectedValues = new Map[3];
		expectedValues[0] = null;
		expectedValues[1] = new HashMap<>();
		expectedValues[2] = new HashMap<>();
		expectedValues[2].put("A", "B");

		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0xFF,
			0x00,
			0x01, 0x01, 0x41, 0x01, 0x42,

			0xFF,
			0x00,
			0x01, 0x01, 0x41, 0x01, 0x42,
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		for (Map<String, String> expected : expectedValues) {
			//act
			final Map<String, String> actual = reader.readMap(Decoder.STRING, Decoder.STRING);

			//assert
			Assert.assertEquals(expected, actual);
		}
		for (Map<String, String> expected : expectedValues) {
			//act
			final Map<String, String> actual = Decoder.forMap(Decoder.STRING, Decoder.STRING).decode(reader);

			//assert
			Assert.assertEquals(expected, actual);
		}

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadArrayOfMap() throws Exception {
		//arrange
		@SuppressWarnings("unchecked")
		final Map<String, String>[] expectedValues = new Map[3];
		expectedValues[0] = null;
		expectedValues[1] = new HashMap<>();
		expectedValues[2] = new HashMap<>();
		expectedValues[2].put("A", "B");

		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0x03,
			0xFF,
			0x00,
			0x01, 0x01, 0x41, 0x01, 0x42,
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		// act
		final Map<String, String>[] actual = reader.readArray(Decoder.forMap(Decoder.STRING, Decoder.STRING));

		//assert
		Assert.assertArrayEquals(expectedValues, actual);

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadArrayOfArrayOfMap() throws Exception {
		//arrange
		@SuppressWarnings("unchecked")
		final Map<String, String>[][] expected = new Map[1][3];
		expected[0][0] = null;
		expected[0][1] = new HashMap<>();
		expected[0][2] = new HashMap<>();
		expected[0][2].put("A", "B");

		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0x01,
			0x03,
			0xFF,
			0x00,
			0x01, 0x01, 0x41, 0x01, 0x42,
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		// act
		final Map<String, String>[][] actual = reader.readArray(Decoder.forArray(Decoder.forMap(Decoder.STRING, Decoder.STRING)));

		//assert
		Assert.assertArrayEquals(expected, actual);

		Assert.assertEquals(0, stream.available());
	}

	@Test
	public void testReadMapOfArray() throws Exception {
		//arrange
		@SuppressWarnings("unchecked")
		final Map<String, String[]> expected = new LinkedHashMap<>();
		expected.put(null, null);
		expected.put("A", new String[0]);
		expected.put("B", new String[]{"A", "B"});

		final byte[] data = Utils.convertIntArrayToByteArray(new int[]{
			0x03,
			0xFF, 0xFF,
			0x01, 0x41, 0x00,
			0x01, 0x42, 0x02, 0x01, 0x41, 0x01, 0x42
		});

		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		final StreamReader reader = new StreamReader(stream);

		// act
		final Map<String, String[]> actual = reader.readMap(Decoder.STRING, Decoder.forArray(Decoder.STRING));

		//assert
		Assert.assertEquals(0, stream.available());

		final List<Map.Entry<String, String[]>> expectedEntries = new ArrayList<>(expected.entrySet());
		final List<Map.Entry<String, String[]>> actualEntries = new ArrayList<>(actual.entrySet());

		int i = 0;
		for (Map.Entry<String, String[]> expectedEntry : expectedEntries) {
			final Map.Entry<String, String[]> actualEntity = actualEntries.get(i++);

			Assert.assertEquals(expectedEntry.getKey(), actualEntity.getKey());
			Assert.assertArrayEquals(expectedEntry.getValue(), actualEntity.getValue());
		}
	}
}