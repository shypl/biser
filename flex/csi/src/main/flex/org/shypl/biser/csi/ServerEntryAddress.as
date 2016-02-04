package org.shypl.biser.csi {
	public class ServerEntryAddress {
		private var _host:String;
		private var _port:int;

		public function ServerEntryAddress(address:String, port:int = -1) {
			if (port == -1) {
				var i:int = address.indexOf(":");
				_host = address.substring(0, i);
				_port = parseInt(address.substring(i + 1));
			}
			else {
				_host = address;
				_port = port;
			}
		}

		public function get host():String {
			return _host;
		}

		public function get port():int {
			return _port;
		}

		public function toString():String {
			return host + ":" + port;
		}
	}
}
