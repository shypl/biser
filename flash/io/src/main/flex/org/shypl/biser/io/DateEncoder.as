package org.shypl.biser.io {
	public final class DateEncoder implements Encoder {
		public static const INSTANCE:Encoder = new DateEncoder();

		public function encode(value:Object, writer:BiserWriter):void {
			writer.writeDate(value as Date);
		}
	}
}
