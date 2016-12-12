package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	
	import org.shypl.common.collection.Deque;
	import org.shypl.common.collection.LinkedList;
	
	public class OutgoingMessages {
		private var _idCounter:int;
		private var list:Deque = new LinkedList();
		private var lastReleasedId:int;
		
		public function OutgoingMessages() {
		}
		
		public function create(data:ByteArray):OutgoingMessage {
			var message:OutgoingMessage = new OutgoingMessage(nextId(), data);
			list.addLast(message);
			return message;
		}
		
		public function releaseTo(id:int):void {
			while (lastReleasedId != id && !list.isEmpty()) {
				var first:OutgoingMessage = list.removeFirst();
				lastReleasedId = first.id;
			}
		}
		
		public function getQueue():Vector.<OutgoingMessage> {
			return list.toVector(OutgoingMessage);
		}
		
		private function nextId():int {
			var id:int = ++_idCounter;
			if (id == 0) {
				id = ++_idCounter;
			}
			return id;
		}
	}
}
