package org.shypl.biser.io {
	public final class UintEncoder implements Encoder {
		public static const INSTANCE:Encoder = new UintEncoder();

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeUint(uint(value));
		}
	}
}
