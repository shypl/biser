package org.shypl.biser.io {
	import org.shypl.asak.lang.AbstractMethodException;
	import org.shypl.asak.util.CollectionUtils;

	[Abstract]
	public class TypedDecoder implements Decoder {
		protected var _type:Class;

		public function TypedDecoder(type:Class) {
			_type = type;
		}

		public function createVector(size:int):Object {
			return CollectionUtils.createVector(_type, size, true);
		}

		[Abstract]
		public function decode(reader:BiserReader):Object {
			throw new AbstractMethodException();
		}
	}
}
