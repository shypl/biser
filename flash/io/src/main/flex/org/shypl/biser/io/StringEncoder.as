package org.shypl.biser.io {
	public final class StringEncoder implements Encoder {
		public static const INSTANCE:Encoder = new StringEncoder();

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeString(value === null ? null : String(value));
		}
	}
}
