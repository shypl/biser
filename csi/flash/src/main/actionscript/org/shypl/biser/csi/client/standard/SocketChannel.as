package org.shypl.biser.csi.client.standard {
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.Socket;
	import flash.system.Security;
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.client.Channel;
	import org.shypl.biser.csi.client.ChannelAcceptor;
	import org.shypl.biser.csi.client.ChannelHandler;
	import org.shypl.common.lang.ErrorEventException;
	import org.shypl.common.net.InetSocketAddress;
	import org.shypl.common.util.callDelayed;

	public class SocketChannel implements Channel {
		private var _socket:Socket;

		private var _acceptor:ChannelAcceptor;
		private var _handler:ChannelHandler;
		private var _opened:Boolean;

		public function SocketChannel(address:InetSocketAddress, acceptor:ChannelAcceptor) {
			_acceptor = acceptor;

			Security.loadPolicyFile("xmlsocket://" + address.host + ":" + address.port);

			_socket = new Socket();
			_socket.addEventListener(Event.CONNECT, onConnect);
			_socket.addEventListener(Event.CLOSE, onClose);
			_socket.addEventListener(IOErrorEvent.IO_ERROR, onError);
			_socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onError);
			_socket.addEventListener(ProgressEvent.SOCKET_DATA, onData);

			try {
				_socket.connect(address.host, address.port);
			}
			catch (e:Error) {
				_acceptor.failOpenChannel(e);
				free();
			}
		}

		public function close():void {
			if (_opened) {
				_opened = false;
				callDelayed(_handler.handleChannelClose);
			}
			free();
		}

		public function writeByte(byte:int):void {
			if (_opened) {
				_socket.writeByte(byte);
				_socket.flush();
			}
		}

		public function writeBytes(bytes:ByteArray):void {
			if (_opened) {
				_socket.writeBytes(bytes);
				_socket.flush();
			}
		}

		private function freeSocket():void {
			if (_socket != null) {
				_socket.removeEventListener(Event.CONNECT, onConnect);
				_socket.removeEventListener(Event.CLOSE, onClose);
				_socket.removeEventListener(IOErrorEvent.IO_ERROR, onError);
				_socket.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onError);
				_socket.removeEventListener(ProgressEvent.SOCKET_DATA, onData);

				try {
					_socket.close();
				}
				catch (e:Error) {
				}
				_socket = null;
			}
		}

		private function free():void {
			freeSocket();
			_socket = null;
			_acceptor = null;
			_handler = null;
		}

		private function onConnect(event:Event):void {
			_opened = true;
			_handler = _acceptor.acceptChannel(this);
		}

		private function onClose(event:Event):void {
			close();
		}

		private function onError(event:ErrorEvent):void {
			var e:ErrorEventException = new ErrorEventException(event);

			if (_opened) {
				_opened = false;
				_handler.handleChannelError(e);
				close();
			}
			else {
				_acceptor.failOpenChannel(e);
				free();
			}
		}

		private function onData(event:ProgressEvent):void {
			if (_opened) {
				var data:ByteArray = new ByteArray();
				_socket.readBytes(data);
				_handler.handleChannelData(data);
			}
		}
	}
}
