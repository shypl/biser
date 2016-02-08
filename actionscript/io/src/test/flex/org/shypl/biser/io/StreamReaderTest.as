package org.shypl.biser.io {
	import flash.utils.ByteArray;

	import org.shypl.asak.collection.LinkedHashMap;
	import org.shypl.asak.collection.Map;
	import org.shypl.asak.collection.MapIterator;
	import org.shypl.asak.lang.Enum;
	import org.shypl.asak.math.Long;
	import org.shypl.flexunit.Assert;

	public class StreamReaderTest {
		private static function compareMaps(expected:Object, actual:Object, valueComparator:Function = null):Boolean {
			if (Assert.isBothNull(expected, actual)) {
				return true;
			}
			if (Assert.isBothClass(expected, actual, Map)) {
				const expectedMap:Map = expected as Map;
				const actualMap:Map = actual as Map;

				if (expectedMap.length == actualMap.length) {
					const expectedItr:MapIterator = expectedMap.iterator();
					const actualItr:MapIterator = actualMap.iterator();

					if (valueComparator == null) {
						valueComparator = Assert.isEquals;
					}

					while (expectedItr.next() && actualItr.next()) {
						if (!valueComparator(expectedItr.key, actualItr.key) || !valueComparator(expectedItr.value, actualItr.value)) {
							return false;
						}
					}

					return true;
				}
			}
			return false;
		}

		[Test]
		public function testReadByte():void {
			//arrange
			const expectedValues:Array = [-128, -1, 0, 1, 127];
			const data:Array = Utils.intsToBytes([
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
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:int;
			var actual:int;
			for each (expected in expectedValues) {
				//act
				actual = reader.readByte();

				//assert
				Assert.assertEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = ByteDecoder.INSTANCE.decode(reader) as int;

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadBool():void {
			//arrange
			const expectedValues:Array = [false, true];
			const data:Array = Utils.intsToBytes([
				0x00, 0x01,
				0x00, 0x01
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Boolean;
			var actual:Boolean;
			for each (expected in expectedValues) {
				//act
				actual = reader.readBool();

				//assert
				Assert.assertEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = BoolDecoder.INSTANCE.decode(reader) as Boolean;

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadInt():void {
			//arrange
			const expectedValues:Array = [-2147483648, -128, -127, -1, 0, 127, 128, 253, 254, 2147483647];
			const data:Array = Utils.intsToBytes([
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
				0xFE, 0x7F, 0xFF, 0xFF, 0xFF // 2147483647
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:int;
			var actual:int;
			for each (expected in expectedValues) {
				//act
				actual = reader.readInt();

				//assert
				Assert.assertEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = IntDecoder.INSTANCE.decode(reader) as int;

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadUint():void {
			//arrange
			const expectedValues:Array = [0, 127, 128, 254, 255, 2147483647, 4294967295];
			const data:Array = Utils.intsToBytes([
				0x00, // 0
				0x7F, // 127
				0x80, // 128
				0xFE, // 254
				0xFF, 0x00, 0x00, 0x00, 0xFF, // 255
				0xFF, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
				0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 4294967295

				0x00, // 0
				0x7F, // 127
				0x80, // 128
				0xFE, // 254
				0xFF, 0x00, 0x00, 0x00, 0xFF, // 255
				0xFF, 0x7F, 0xFF, 0xFF, 0xFF, // 2147483647
				0xFF, 0xFF, 0xFF, 0xFF, 0xFF // 4294967295
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:uint;
			var actual:uint;
			for each (expected in expectedValues) {
				//act
				actual = reader.readUint();

				//assert
				Assert.assertEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = UintDecoder.INSTANCE.decode(reader) as uint;

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadLong():void {
			//arrange
			const expectedValues:Array = [
				Long.valueOf("-9223372036854775808"), Long.valueOf(-2147483648), Long.valueOf(-128), Long.valueOf(-127), Long.valueOf(-1), Long.valueOf(0),
				Long.valueOf(127), Long.valueOf(128), Long.valueOf(253), Long.valueOf(254), Long.valueOf(2147483647), Long.valueOf("9223372036854775807")
			];
			const data:Array = Utils.intsToBytes([
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
				0xFE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF // 9223372036854775807
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Long;
			var actual:Long;
			for each (expected in expectedValues) {
				//act
				actual = reader.readLong();

				//assert
				Assert.assertEquals(expected.toString(), actual.toString());
			}
			for each (expected in expectedValues) {
				//act
				actual = LongDecoder.INSTANCE.decode(reader) as Long;

				//assert
				Assert.assertEquals(expected.toString(), actual.toString());
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadUlong():void {
			//arrange
			const expectedValues:Array = [
				Long.valueOf("-9223372036854775808"), Long.valueOf(-2147483648), Long.valueOf(-128), Long.valueOf(-127), Long.valueOf(-1), Long.valueOf(0),
				Long.valueOf(127), Long.valueOf(128), Long.valueOf(253), Long.valueOf(254), Long.valueOf(2147483647), Long.valueOf("9223372036854775807")
			];
			const data:Array = Utils.intsToBytes([
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
				0xFE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF // 9223372036854775807
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Long;
			var actual:Long;
			for each (expected in expectedValues) {
				//act
				actual = reader.readUlong();

				//assert
				Assert.assertEquals(expected.toString(), actual.toString());
			}
			for each (expected in expectedValues) {
				//act
				actual = UlongDecoder.INSTANCE.decode(reader) as Long;

				//assert
				Assert.assertEquals(expected.toString(), actual.toString());
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadDouble():void {
			//arrange
			const expectedValues:Array = [
				0, 1, -1, 1.5, -1.5,
				2.2250738585072014E-308, Number.MIN_VALUE, Number.MAX_VALUE,
				Number.NaN, Number.NEGATIVE_INFINITY, Number.POSITIVE_INFINITY
			];
			const data:Array = Utils.intsToBytes([
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
				0x7F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 // POSITIVE_INFINITY
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Number;
			var actual:Number;
			for each (expected in expectedValues) {
				//act
				actual = reader.readDouble();

				//assert
				Assert.assertEquals(String(expected), String(actual));
			}
			for each (expected in expectedValues) {
				//act
				actual = DoubleDecoder.INSTANCE.decode(reader) as Number;

				//assert
				Assert.assertEquals(String(expected), String(actual));
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadBytes():void {
			//arrange
			const expectedValues:Array = [
				null,
				Utils.wrapBytes([]),
				Utils.wrapBytes([-128, -1, 0, 1, 127])
			];
			const data:Array = Utils.intsToBytes([
				0xFF, // null
				0x00, // {}
				0x05, 0x80, 0xFF, 0x00, 0x01, 0x7F, // {-128, -1, 0, 1, 127}

				0xFF, // null
				0x00, // {}
				0x05, 0x80, 0xFF, 0x00, 0x01, 0x7F // {-128, -1, 0, 1, 127}
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:ByteArray;
			var actual:ByteArray;
			for each (expected in expectedValues) {
				//act
				actual = reader.readBytes();

				//assert
				Assert.assertArrayEquals(Utils.extractBytes(expected), Utils.extractBytes(actual));
			}
			for each (expected in expectedValues) {
				//act
				actual = BytesDecoder.INSTANCE.decode(reader) as ByteArray;

				//assert
				Assert.assertArrayEquals(Utils.extractBytes(expected), Utils.extractBytes(actual));
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadString():void {
			//arrange
			const expectedValues:Array = [
				null,
				"",
				"Hello World!",
				"\n \t \r 1234567890±!@#$%^&*()_+<>,./?{}[];\'\\:\"|",
				"A pile of poo: \uD83D\uDCA9.",
				"你好世界！",
				"مرحبا العالم!"
			];
			const data:Array = Utils.intsToBytes([
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
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:String;
			var actual:String;
			for each (expected in expectedValues) {
				//act
				actual = reader.readString();

				//assert
				Assert.assertEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = StringDecoder.INSTANCE.decode(reader) as String;

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadDate():void {
			//arrange
			const expectedValues:Array = [null, new Date(0), new Date(-100000), new Date(100000), new Date(int.MAX_VALUE)];
			const data:Array = Utils.intsToBytes([
				0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // null
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
				0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0x79, 0x60, // -100000
				0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x86, 0xA0, // 100000
				0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF, // int.MAX_VALUE

				0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // null
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 0
				0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0x79, 0x60, // -100000
				0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x86, 0xA0, // 100000
				0x00, 0x00, 0x00, 0x00, 0x7F, 0xFF, 0xFF, 0xFF // int.MAX_VALUE
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Date;
			var actual:Date;
			for each (expected in expectedValues) {
				//act
				actual = reader.readDate();

				//assert
				Assert.assertEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = DateDecoder.INSTANCE.decode(reader) as Date;

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadEnum():void {
			//arrange
			const expectedValues:Array = [null, EnumStub.VALUE];
			const data:Array = Utils.intsToBytes([
				0xFF, 0x00
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Enum;
			var actual:Enum;
			for each (expected in expectedValues) {
				//act
				actual = reader.readEnum(EnumStub);

				//assert
				Assert.assertEquals(expected, actual);
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadEntity():void {
			//arrange
			const expectedValues:Array = [null, new EntityStub()];
			const data:Array = Utils.intsToBytes([
				0xFF, 0x00
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Entity;
			var actual:Entity;
			for each (expected in expectedValues) {
				//act
				actual = reader.readEntity(EntityStub);

				//assert
				Assert.assertEqualsWithComparing(expected, actual, function (expected:Object, actual:Object):Boolean {
					return Assert.isBothNull(expected, actual) || (expected is EntityStub && actual is EntityStub);
				});
			}

			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadArray():void {
			//arrange
			const expectedValues:Array = [null, new <String>[], new <String>["A", "B"]];
			const data:Array = Utils.intsToBytes([
				0xFF,
				0x00,
				0x02, 0x01, 0x41, 0x01, 0x42,

				0xFF,
				0x00,
				0x02, 0x01, 0x41, 0x01, 0x42
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Vector.<String>;
			var actual:Vector.<String>;
			for each (expected in expectedValues) {
				//act
				actual = reader.readArray(StringDecoder.INSTANCE) as Vector.<String>;

				//assert
				Assert.assertArrayEquals(expected, actual);
			}
			for each (expected in expectedValues) {
				//act
				actual = ArrayDecoder.factory(StringDecoder.INSTANCE).decode(reader) as Vector.<String>;

				//assert
				Assert.assertArrayEquals(expected, actual);
			}
			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadMap():void {
			//arrange
			const expectedValues:Array = [null, new LinkedHashMap(), new LinkedHashMap()];
			expectedValues[2].put("A", "B");

			const data:Array = Utils.intsToBytes([
				0xFF,
				0x00,
				0x01, 0x01, 0x41, 0x01, 0x42,

				0xFF,
				0x00,
				0x01, 0x01, 0x41, 0x01, 0x42
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			var expected:Map;
			var actual:Map;
			for each (expected in expectedValues) {
				//act
				actual = reader.readMap(StringDecoder.INSTANCE, StringDecoder.INSTANCE);

				//assert
				Assert.assertEqualsWithComparing(expected, actual, compareMaps);
			}
			for each (expected in expectedValues) {
				//act
				actual = Map(MapDecoder.factory(StringDecoder.INSTANCE, StringDecoder.INSTANCE).decode(reader));

				//assert
				Assert.assertEqualsWithComparing(expected, actual, compareMaps);
			}
			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadArrayOfMap():void {
			//arrange
			const expected:Array = [null, new LinkedHashMap(), new LinkedHashMap()];
			expected[2].put("A", "B");

			const data:Array = Utils.intsToBytes([
				0x03,
				0xFF,
				0x00,
				0x01, 0x01, 0x41, 0x01, 0x42
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			// act
			var actual:Vector.<Map> = reader.readArray(MapDecoder.factory(StringDecoder.INSTANCE, StringDecoder.INSTANCE)) as Vector.<Map>;

			//assert
			Assert.assertArrayEqualsWithComparing(expected, actual, compareMaps);
			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadArrayOfArrayOfMap():void {
			//arrange
			const expected:Array = [[null, new LinkedHashMap(), new LinkedHashMap()]];
			expected[0][2].put("A", "B");

			const data:Array = Utils.intsToBytes([
				0x01,
				0x03,
				0xFF,
				0x00,
				0x01, 0x01, 0x41, 0x01, 0x42
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			// act
			var actual:Vector.<Vector.<Map>> = reader.readArray(ArrayDecoder.factory(MapDecoder.factory(StringDecoder.INSTANCE, StringDecoder.INSTANCE)))
				as Vector.<Vector.<Map>>;

			//assert
			Assert.assertArrayEqualsWithComparing(expected, actual, function (expected:Object, actual:Object):Boolean {
				return Assert.isArrayEqualsWithComparing(expected, actual, compareMaps);
			});
			Assert.assertEquals(0, stream.bytesAvailable);
		}

		[Test]
		public function testReadMapOfArray():void {
			//arrange
			const expected:Map = new LinkedHashMap();
			expected.put(null, null);
			expected.put("A", new <String>[]);
			expected.put("B", new <String>["A", "B"]);

			const data:Array = Utils.intsToBytes([
				0x03,
				0xFF, 0xFF,
				0x01, 0x41, 0x00,
				0x01, 0x42, 0x02, 0x01, 0x41, 0x01, 0x42
			]);

			const stream:ByteArray = Utils.wrapBytes(data);
			const reader:StreamReader = new StreamReader(stream);

			// act
			var actual:Map = reader.readMap(StringDecoder.INSTANCE, ArrayDecoder.factory(StringDecoder.INSTANCE));

			//assert
			Assert.assertEqualsWithComparing(expected, actual, function (expected:Object, actual:Object):Boolean {
				return compareMaps(expected, actual, Assert.isArrayEquals);
			});
			Assert.assertEquals(0, stream.bytesAvailable);
		}
	}
}
