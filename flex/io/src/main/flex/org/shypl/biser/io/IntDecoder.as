package org.shypl.biser.io {
	public final class IntDecoder implements Decoder {
		public static const INSTANCE:Decoder = new IntDecoder();

		public function createVector(size:int):Object {
			return new Vector.<int>(size, true);
		}

		public function decode(reader:BiserReader):Object {
			return reader.readInt();
		}
	}
}
