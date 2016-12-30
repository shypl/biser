package org.shypl.biser.io {
	import org.shypl.common.math.Long;
	
	public final class UlongEncoder implements Encoder {
		public static const INSTANCE:Encoder = new UlongEncoder();
		
		public function encode(value:Object, writer:DataWriter):void {
			writer.writeUlong(Long(value));
		}
	}
}
