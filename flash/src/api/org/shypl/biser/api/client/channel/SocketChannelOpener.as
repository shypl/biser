package org.shypl.biser.api.client.channel {
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.Socket;
	import flash.system.Security;

	import org.shypl.biser.api.ServerEntryAddress;
	import org.shypl.biser.api.client.ChannelOpenHandler;
	import org.shypl.common.lang.ErrorEventException;
	import org.shypl.common.lang.IllegalStateException;

	public class SocketChannelOpener {
		private var _socket:Socket;
		private var _handler:ChannelOpenHandler;


		public function SocketChannelOpener(address:ServerEntryAddress, handler:ChannelOpenHandler) {
			_handler = handler;

			Security.loadPolicyFile("xmlsocket://" + address.host + ":" + address.port);

			_socket = new Socket();
			_socket.addEventListener(Event.CONNECT, onConnect);
			_socket.addEventListener(Event.CLOSE, onClose);
			_socket.addEventListener(IOErrorEvent.IO_ERROR, onError);
			_socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onError);

			try {
				_socket.connect(address.host, address.port);
			}
			catch (e:Error) {
				dispatchError(new IllegalStateException("Cannot connect to socket", e));
			}
		}

		private function destroy():void {
			removeEventListeners();
			_socket = null;
			_handler = null;
		}

		private function removeEventListeners():void {
			_socket.removeEventListener(Event.CONNECT, onConnect);
			_socket.removeEventListener(Event.CLOSE, onClose);
			_socket.removeEventListener(IOErrorEvent.IO_ERROR, onError);
			_socket.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onError);
		}

		private function dispatchError(error:Error):void {
			removeEventListeners();
			try {
				_socket.close();
			}
			catch (e:Error) {
			}
			_handler.handleError(error);
			destroy();
		}

		private function onConnect(event:Event):void {
			new SocketChannel(_socket, _handler);
			destroy();
		}

		private function onClose(event:Event):void {
			dispatchError(new IllegalStateException("Unexpected closing of socket"));
		}

		private function onError(event:ErrorEvent):void {
			dispatchError(new ErrorEventException(event));
		}
	}
}
