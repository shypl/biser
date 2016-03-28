package org.shypl.biser.io {
	import flash.utils.ByteArray;

	public final class BytesEncoder implements Encoder {
		public static const INSTANCE:Encoder = new BytesEncoder();

		public function encode(value:Object, writer:DataWriter):void {
			writer.writeBytes(ByteArray(value));
		}
	}
}
