package org.shypl.biser.io {
	public final class ByteDecoder implements Decoder {
		public static const INSTANCE:Decoder = new ByteDecoder();

		public function createVector(size:int):Object {
			return new Vector.<int>(size, true);
		}

		public function decode(reader:BiserReader):Object {
			return reader.readByte();
		}
	}
}
