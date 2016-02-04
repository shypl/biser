package org.shypl.biser.io {
	import org.shypl.common.collection.LinkedHashMap;
	import org.shypl.common.collection.Map;

	[Abstract]
	public class Entity {
		public function toString():String {
			var fields:Map = new LinkedHashMap();
			_toString(fields);
			return fields.toString();
		}

		protected function _id():int {
			return 0;
		}

		protected function _encode(writer:BiserWriter):void {
		}

		protected function _decode(reader:BiserReader):void {
		}

		protected function _toString(fields:Map):void {
		}

		internal final function _id0():int {
			return _id();
		}

		internal final function _encode0(writer:BiserWriter):void {
			_encode(writer);
		}

		internal final function _decode0(reader:BiserReader):void {
			_decode(reader);
		}
	}
}
