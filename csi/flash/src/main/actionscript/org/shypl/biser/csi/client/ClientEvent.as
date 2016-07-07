package org.shypl.biser.csi.client {
	import flash.events.Event;

	public class ClientEvent extends Event {
		public static const CLIENT_CONNECTED:String = "clientConnected";
		public static const CLIENT_CONNECTION_ESTABLISHED:String = "clientConnectionEstablished";
		public static const CLIENT_CONNECTION_INTERRUPTED:String = "clientConnectionInterrupted";

		public function ClientEvent(type:String) {
			super(type);
		}
	}
}
