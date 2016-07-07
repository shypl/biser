package org.shypl.biser.io {
	public final class BoolDecoder implements Decoder {
		public static const INSTANCE:Decoder = new BoolDecoder();

		public function createVector(size:int):Object {
			return new Vector.<Boolean>(size, true);
		}

		public function decode(reader:DataReader):Object {
			return reader.readBool();
		}
	}
}
