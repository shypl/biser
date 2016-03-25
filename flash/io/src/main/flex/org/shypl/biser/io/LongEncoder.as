package org.shypl.biser.io {
	import org.shypl.common.math.Long;

	public final class LongEncoder implements Encoder {
		public static const INSTANCE:Encoder = new LongEncoder();

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeLong(Long(value));
		}
	}
}
