package org.shypl.biser.io {
	import flash.utils.Dictionary;

	public class ArrayEncoder implements Encoder {
		private static const cache:Dictionary = new Dictionary();

		public static function factory(elementEncoder:Encoder):Encoder {
			var arrayEncoder:Encoder = cache[elementEncoder];
			if (arrayEncoder == null) {
				arrayEncoder = new ArrayEncoder(elementEncoder);
				cache[elementEncoder] = arrayEncoder;
			}
			return arrayEncoder;
		}

		private var _elementEncoder:Encoder;

		public function ArrayEncoder(elementEncoder:Encoder) {
			_elementEncoder = elementEncoder;
		}

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeArray(value, _elementEncoder);
		}
	}
}
