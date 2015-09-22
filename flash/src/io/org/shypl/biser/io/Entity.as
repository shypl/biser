package org.shypl.biser.io {
	[Abstract]
	public class Entity {
		internal final function _id0():int {
			return _id();
		}

		internal final function _encode0(writer:BiserWriter):void {
			_encode(writer);
		}

		internal final function _decode0(reader:BiserReader):void {
			_decode(reader);
		}

		protected function _id():int {
			return 0;
		}

		protected function _encode(writer:BiserWriter):void {
		}

		protected function _decode(reader:BiserReader):void {
		}
	}
}
