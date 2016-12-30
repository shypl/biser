package org.shypl.biser.io {
	import org.shypl.common.math.Long;
	
	public final class UlongDecoder implements Decoder {
		public static const INSTANCE:Decoder = new UlongDecoder();
		
		public function createVector(size:int):Object {
			return new Vector.<Long>(size, true);
		}
		
		public function decode(reader:DataReader):Object {
			return reader.readUlong();
		}
	}
}
