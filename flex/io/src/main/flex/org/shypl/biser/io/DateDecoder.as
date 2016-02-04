package org.shypl.biser.io {
	public final class DateDecoder implements Decoder {
		public static const INSTANCE:Decoder = new DateDecoder();

		public function createVector(size:int):Object {
			return new Vector.<Date>(size, true);
		}

		public function decode(reader:BiserReader):Object {
			return reader.readDate();
		}
	}
}
