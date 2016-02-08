package org.shypl.biser.io {
	import org.shypl.asak.math.Long;

	public final class LongEncoder implements Encoder {
		public static const INSTANCE:Encoder = new LongEncoder();

		public function encode(value:Object, writer:BiserWriter):void {
			writer.writeLong(Long(value));
		}
	}
}
