package org.shypl.biser.io {
	import org.shypl.common.lang.Enum;

	public class EnumStub extends Enum {
		public static const VALUE:EnumStub = new EnumStub("VALUE");

		public function EnumStub(name:String) {
			super(name);
		}
	}
}
