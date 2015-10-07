package org.shypl.biser.io {
	import flash.utils.ByteArray;
	import flash.utils.IDataOutput;

	import org.shypl.common.collection.Map;
	import org.shypl.common.collection.MapIterator;
	import org.shypl.common.lang.Enum;
	import org.shypl.common.lang.IllegalArgumentException;
	import org.shypl.common.lang.NullPointerException;
	import org.shypl.common.lang.OutOfRangeException;
	import org.shypl.common.math.Long;
	import org.shypl.common.util.CollectionUtils;

	public class StreamWriter implements BiserWriter {
		private var _stream:IDataOutput;

		public function StreamWriter(stream:IDataOutput) {
			_stream = stream;
		}

		public function writeByte(value:int):void {
			_stream.writeByte(value);
		}

		public function writeBool(value:Boolean):void {
			_stream.writeByte(value ? 0x01 : 0x00);
		}

		public function writeInt(value:int):void {
			if (value === -1) {
				_stream.writeByte(0xFF);
			}
			else if (value >= 0 && value <= 253) {
				_stream.writeByte(value);
			}
			else {
				_stream.writeByte(0xFE);
				_stream.writeInt(value);
			}
		}

		public function writeUint(value:uint):void {
			if (value >= 0 && value <= 254) {
				_stream.writeByte(value);
			}
			else {
				_stream.writeByte(0xFE);
				_stream.writeUnsignedInt(value);
			}
		}

		public function writeLong(value:Long):void {
			if (value === null) {
				throw new NullPointerException();
			}

			if (value.isNegativeOne()) {
				_stream.writeByte(0xFF);
			}
			else if (value.compareTo(0) >= 0 && value.compareTo(253) <= 0) {
				_stream.writeByte(value.lowBits);
			}
			else {
				_stream.writeByte(0xFE);
				_stream.writeUnsignedInt(value.highBits);
				_stream.writeUnsignedInt(value.lowBits);
			}
		}

		public function writeUlong(value:Long):void {
			writeLong(value);
		}

		public function writeDouble(value:Number):void {
			if (isNaN(value)) {
				_stream.writeInt(0x7FF80000);
				_stream.writeInt(0);
			}
			else {
				_stream.writeDouble(value);
			}
		}

		public function writeBytes(value:ByteArray):void {
			if (value === null) {
				writeInt(-1);
			}
			else {
				writeInt(value.length);
				_stream.writeBytes(value);
			}
		}

		public function writeString(value:String):void {
			if (value === null) {
				writeInt(-1);
			}
			else if (value.length === 0) {
				writeInt(0);
			}
			else {
				const utfBytes:ByteArray = new ByteArray();
				utfBytes.writeUTFBytes(value);
				writeInt(utfBytes.length);
				_stream.writeBytes(utfBytes);
			}
		}

		public function writeDate(value:Date):void {
			const ms:Long = value === null ? Long.MIN_VALUE : Long.valueOfNumber(value.getTime());
			_stream.writeUnsignedInt(ms.highBits);
			_stream.writeUnsignedInt(ms.lowBits);
		}

		public function writeEnum(value:Enum):void {
			writeInt(value === null ? -1 : value.ordinal);
		}

		public function writeEntity(value:Entity):void {
			if (value === null) {
				writeInt(-1);
			}
			else {
				writeInt(value._id0());
				value._encode0(this);
			}
		}

		public function writeArray(array:Object, elementEncoder:Encoder):void {
			if (array === null) {
				writeInt(-1);
			}
			else {
				if (!CollectionUtils.isArrayOrVector(array)) {
					throw new IllegalArgumentException("Value must be an Array or Vector object");
				}

				var length:uint = array.length;
				if (length > int.MAX_VALUE) {
					throw new OutOfRangeException("Max array length " + int.MAX_VALUE);
				}

				writeInt(length);
				for (var i:uint = 0; i < length; ++i) {
					elementEncoder.encode(array[i], this);
				}
			}
		}

		public function writeMap(map:Map, keyEncoder:Encoder, valueEncoder:Encoder):void {
			if (map === null) {
				writeInt(-1);
			}
			else {
				writeInt(map.size);
				if (map.size !== 0) {
					const it:MapIterator = map.iterator();
					while (it.next()) {
						keyEncoder.encode(it.key, this);
						valueEncoder.encode(it.value, this);
					}
				}
			}
		}
	}
}
