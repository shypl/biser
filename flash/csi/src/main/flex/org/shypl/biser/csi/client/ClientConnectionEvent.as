package org.shypl.biser.csi.client {
	import flash.events.Event;

	public class ClientConnectionEvent extends Event {
		public static const CLIENT_CONNECTION_ESTABLISHED:String = "clientConnectionEstablished";
		public static const CLIENT_CONNECTION_INTERRUPTED:String = "clientConnectionInterrupted";

		public function ClientConnectionEvent(type:String) {
			super(type);
		}
	}
}
