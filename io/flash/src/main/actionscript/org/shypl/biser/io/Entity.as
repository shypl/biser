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
		
		public function get biserEntityClassId():int {
			return 0;
		}
		
		protected function _encode(writer:DataWriter):void {
		}
		
		protected function _decode(reader:DataReader):void {
		}
		
		protected function _toString(fields:Map):void {
		}
		
		internal final function _encode0(writer:DataWriter):void {
			_encode(writer);
		}
		
		internal final function _decode0(reader:DataReader):void {
			_decode(reader);
		}
	}
}
