package org.shypl.biser.io {
	public final class IntEncoder implements Encoder {
		public static const INSTANCE:Encoder = new IntEncoder();

		public function encode(value:Object, writer:BiserWriter):void {
			writer.writeInt(int(value));
		}
	}
}
