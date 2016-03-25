package org.shypl.biser.io {
	import org.shypl.common.collection.Map;

	public class MapEncoder implements Encoder {
		public static function factory(keyEncoder:Encoder, valueEncoder:Encoder):Encoder {
			return new MapEncoder(keyEncoder, valueEncoder);
		}

		private var _keyEncoder:Encoder;
		private var _valueEncoder:Encoder;

		public function MapEncoder(keyEncoder:Encoder, valueEncoder:Encoder) {
			_keyEncoder = keyEncoder;
			_valueEncoder = valueEncoder;
		}

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeMap(Map(value), _keyEncoder, _valueEncoder);
		}
	}
}
