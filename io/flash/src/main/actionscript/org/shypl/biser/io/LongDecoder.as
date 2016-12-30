package org.shypl.biser.io {
	import org.shypl.common.math.Long;
	
	public final class LongDecoder implements Decoder {
		public static const INSTANCE:Decoder = new LongDecoder();
		
		public function createVector(size:int):Object {
			return new Vector.<Long>(size, true);
		}
		
		public function decode(reader:DataReader):Object {
			return reader.readLong();
		}
	}
}
