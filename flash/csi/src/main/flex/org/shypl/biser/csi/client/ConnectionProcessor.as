package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.ConnectionCloseReason;

	internal class ConnectionProcessor {
		private var _connection:Connection;

		protected function get connection():Connection {
			return _connection;
		}

		public function init(connection:Connection):void {
			_connection = connection;
		}

		public function destroy():void {
			_connection = null;
		}

		public function processAccept():void {
		}

		public function processData():void {
		}

		public function processClose():void {
		}

		protected function closeConnection(flag:int):void {
			var reason:ConnectionCloseReason = ConnectionCloseReason.getReason(flag);
			if (reason == null) {
				_connection.logger.error("ConnectionProcessor: Undefined flag for close {}", flag);
				reason = ConnectionCloseReason.PROTOCOL_BROKEN;
			}
			connection.close(reason);
		}
	}
}
