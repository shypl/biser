package org.shypl.biser.io {
	public final class DoubleEncoder implements Encoder {
		public static const INSTANCE:Encoder = new DoubleEncoder();

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeDouble(Number(value));
		}
	}
}
