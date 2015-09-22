package org.shypl.biser.io {
	public final class ByteEncoder implements Encoder {
		public static const INSTANCE:Encoder = new ByteEncoder();

		public function encode(value:Object, writer:BiserWriter):void {
			writer.writeByte(int(value));
		}
	}
}
