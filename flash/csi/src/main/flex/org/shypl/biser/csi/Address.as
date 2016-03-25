package org.shypl.biser.csi {
	import org.shypl.common.lang.IllegalArgumentException;
	import org.shypl.common.net.InetSocketAddress;

	public class Address {
		private var _socket:InetSocketAddress;

		public function Address(address:Object) {
			if (address is InetSocketAddress) {
				_socket = InetSocketAddress(address);
			}
			else if (address is String) {
				var s:String = (address as String);
				var p:int = s.indexOf(":");
				_socket = new InetSocketAddress(s.substring(0, p), parseInt(s.substring(p + 1)));
			}
			else {
				throw new IllegalArgumentException();
			}
		}

		public function get socket():InetSocketAddress {
			return _socket;
		}

		public function isSocket():Boolean {
			return _socket != null;
		}

		public function toString():String {
			if (isSocket()) {
				return _socket.toString();
			}

			return "<undefined>";
		}
	}

}
