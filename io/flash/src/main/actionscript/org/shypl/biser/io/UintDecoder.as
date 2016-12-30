package org.shypl.biser.io {
	public final class UintDecoder implements Decoder {
		public static const INSTANCE:Decoder = new UintDecoder();
		
		public function createVector(size:int):Object {
			return new Vector.<uint>(size, true);
		}
		
		public function decode(reader:DataReader):Object {
			return reader.readUint();
		}
	}
}
