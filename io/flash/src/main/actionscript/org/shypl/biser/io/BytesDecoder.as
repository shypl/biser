package org.shypl.biser.io {
	import flash.utils.ByteArray;
	
	public final class BytesDecoder implements Decoder {
		public static const INSTANCE:Decoder = new BytesDecoder();
		
		public function createVector(size:int):Object {
			return new Vector.<ByteArray>(size, true);
		}
		
		public function decode(reader:DataReader):Object {
			return reader.readBytes();
		}
	}
}
