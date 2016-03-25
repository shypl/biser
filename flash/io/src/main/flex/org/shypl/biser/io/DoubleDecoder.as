package org.shypl.biser.io {
	public final class DoubleDecoder implements Decoder {
		public static const INSTANCE:Decoder = new DoubleDecoder();

		public function createVector(size:int):Object {
			return new Vector.<Number>(size, true);
		}

		public function decode(reader:DataReader):Object {
			return reader.readDouble();
		}
	}
}
