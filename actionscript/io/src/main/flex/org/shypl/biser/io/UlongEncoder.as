package org.shypl.biser.io {
	import org.shypl.asak.math.Long;

	public final class UlongEncoder implements Encoder {
		public static const INSTANCE:Encoder = new UlongEncoder();

		public function encode(value:Object, writer:BiserWriter):void {
			writer.writeUlong(Long(value));
		}
	}
}
