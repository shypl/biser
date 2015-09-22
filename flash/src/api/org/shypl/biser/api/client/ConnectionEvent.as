package org.shypl.biser.api.client {
	import flash.events.Event;

	public class ConnectionEvent extends Event {
		public static const CONNECTION_ACTIVE:String = "CONNECTION_ACTIVE";
		public static const CONNECTION_INACTIVE:String = "CONNECTION_INACTIVE";

		public function ConnectionEvent(type:String) {
			super(type);
		}
	}
}
