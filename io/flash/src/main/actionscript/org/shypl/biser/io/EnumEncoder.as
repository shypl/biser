package org.shypl.biser.io {
	import org.shypl.common.lang.Enum;
	
	public final class EnumEncoder implements Encoder {
		public static const INSTANCE:Encoder = new EnumEncoder();
		
		public function encode(value:Object, writer:DataWriter):void {
			writer.writeEnum(Enum(value));
		}
	}
}
