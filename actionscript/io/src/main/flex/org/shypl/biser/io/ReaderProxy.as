package org.shypl.biser.io {
	import flash.utils.ByteArray;

	import org.shypl.asak.collection.Map;
	import org.shypl.asak.lang.Enum;
	import org.shypl.asak.math.Long;

	public class ReaderProxy implements BiserReader {
		private var _reader:BiserReader;

		public function ReaderProxy(reader:BiserReader) {
			_reader = reader;
		}

		public function readByte():int {
			return _reader.readByte();
		}

		public function readBool():Boolean {
			return _reader.readBool();
		}

		public function readInt():int {
			return _reader.readInt();
		}

		public function readUint():uint {
			return _reader.readUint();
		}

		public function readLong():Long {
			return _reader.readLong();
		}

		public function readUlong():Long {
			return _reader.readUlong();
		}

		public function readDouble():Number {
			return _reader.readDouble();
		}

		public function readBytes():ByteArray {
			return _reader.readBytes();
		}

		public function readString():String {
			return _reader.readString();
		}

		public function readDate():Date {
			return _reader.readDate();
		}

		public function readEnum(type:Class):Enum {
			return _reader.readEnum(type);
		}

		public function readEntity(type:Class):Entity {
			return _reader.readEntity(type);
		}

		public function readArray(elementDecoder:Decoder):Object {
			return _reader.readArray(elementDecoder);
		}

		public function readMap(keyDecoder:Decoder, valueDecoder:Decoder):Map {
			return _reader.readMap(keyDecoder, valueDecoder);
		}
	}
}
