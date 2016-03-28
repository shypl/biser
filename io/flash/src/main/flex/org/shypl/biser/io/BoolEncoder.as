package org.shypl.biser.io {
	public final class BoolEncoder implements Encoder {
		public static const INSTANCE:Encoder = new BoolEncoder();

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeBool(Boolean(value));
		}
	}
}
