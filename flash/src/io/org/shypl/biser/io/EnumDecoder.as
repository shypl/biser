package org.shypl.biser.io {
	import org.shypl.common.lang.Enum;

	public class EnumDecoder extends TypedDecoder {

		public function EnumDecoder(type:Class) {
			super(type);
		}

		override final public function decode(reader:BiserReader):Object {
			var ordinal:int = reader.readInt();
			return ordinal == -1 ? null : Enum.valueOfOrdinal(_type, ordinal);
		}
	}
}
