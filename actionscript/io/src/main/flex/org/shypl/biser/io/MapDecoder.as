package org.shypl.biser.io {
	import org.shypl.asak.collection.Map;

	public class MapDecoder implements Decoder {
		public static function factory(keyDecoder:Decoder, valueDecoder:Decoder):Decoder {
			return new MapDecoder(keyDecoder, valueDecoder);
		}

		private var _keyDecoder:Decoder;
		private var _valueDecoder:Decoder;

		public function MapDecoder(keyDecoder:Decoder, valueDecoder:Decoder) {
			_keyDecoder = keyDecoder;
			_valueDecoder = valueDecoder;
		}

		public function createVector(size:int):Object {
			return new Vector.<Map>(size, true);
		}

		public function decode(reader:BiserReader):Object {
			return reader.readMap(_keyDecoder, _valueDecoder);
		}
	}
}
