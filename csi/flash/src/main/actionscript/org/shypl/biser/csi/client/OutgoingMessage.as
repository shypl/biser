package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	
	public class OutgoingMessage {
		private var _id:int;
		private var _data:ByteArray;
		
		public function OutgoingMessage(id:int, data:ByteArray) {
			_id = id;
			_data = data;
		}
		
		public function get id():int {
			return _id;
		}
		
		public function get data():ByteArray {
			return _data;
		}
	}
}
