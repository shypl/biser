package org.shypl.biser.csi.client {
	import flash.events.Event;

	public class ClientEvent extends Event {
		public static const CONNECTION_ESTABLISHED:String = "connectionEstablished";
		public static const CONNECTION_INTERRUPTED:String = "connectionInterrupted";

		public function ClientEvent(type:String) {
			super(type);
		}
	}
}
