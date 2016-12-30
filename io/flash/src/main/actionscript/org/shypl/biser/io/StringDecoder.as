package org.shypl.biser.io {
	public final class StringDecoder implements Decoder {
		public static const INSTANCE:Decoder = new StringDecoder();
		
		public function createVector(size:int):Object {
			return new Vector.<String>(size, true);
		}
		
		public function decode(reader:DataReader):Object {
			return reader.readString();
		}
	}
}
