package org.shypl.biser.io {
	import flash.utils.ByteArray;

	import org.shypl.asak.collection.Map;
	import org.shypl.asak.lang.Enum;
	import org.shypl.asak.math.Long;

	public class ByteArrayWriter implements BiserWriter {
		private var _array:ByteArray;
		private var _writer:BiserWriter;

		public function ByteArrayWriter() {
			_array = new ByteArray();
			_writer = new StreamWriter(_array);
		}

		public function toByteArray():ByteArray {
			var array:ByteArray = new ByteArray();
			array.writeBytes(_array);
			array.position = 0;
			return array;
		}

		public function writeByte(value:int):void {
			_writer.writeByte(value);
		}

		public function writeBool(value:Boolean):void {
			_writer.writeBool(value);
		}

		public function writeInt(value:int):void {
			_writer.writeInt(value);
		}

		public function writeUint(value:uint):void {
			_writer.writeUint(value);
		}

		public function writeLong(value:Long):void {
			_writer.writeLong(value);
		}

		public function writeUlong(value:Long):void {
			_writer.writeUlong(value);
		}

		public function writeDouble(value:Number):void {
			_writer.writeDouble(value);
		}

		public function writeBytes(value:ByteArray):void {
			_writer.writeBytes(value);
		}

		public function writeString(value:String):void {
			_writer.writeString(value);
		}

		public function writeDate(value:Date):void {
			_writer.writeDate(value);
		}

		public function writeEnum(value:Enum):void {
			_writer.writeEnum(value);
		}

		public function writeEntity(value:Entity):void {
			_writer.writeEntity(value);
		}

		public function writeArray(array:Object, elementEncoder:Encoder):void {
			_writer.writeArray(array, elementEncoder);
		}

		public function writeMap(map:Map, keyEncoder:Encoder, valueEncoder:Encoder):void {
			_writer.writeMap(map, keyEncoder, valueEncoder);
		}
	}
}
