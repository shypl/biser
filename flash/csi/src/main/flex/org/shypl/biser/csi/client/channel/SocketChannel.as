package org.shypl.biser.csi.client.channel {
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.net.Socket;
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.client.Channel;
	import org.shypl.biser.csi.client.ChannelHandler;
	import org.shypl.biser.csi.client.ChannelOpenHandler;
	import org.shypl.common.lang.ErrorEventException;
	import org.shypl.common.logging.LogManager;
	import org.shypl.common.logging.Logger;
	import org.shypl.common.util.HexUtils;

	public class SocketChannel implements Channel {
		private static const LOGGER:Logger = LogManager.getLoggerByClass(SocketChannel);

		private var _socket:Socket;
		private var _handler:ChannelHandler;

		public function SocketChannel(socket:Socket, handler:ChannelOpenHandler) {
			_socket = socket;
			_handler = handler.handleOpen(this);

			_socket.addEventListener(ProgressEvent.SOCKET_DATA, onSocketData);
			_socket.addEventListener(Event.CLOSE, onClose);
			_socket.addEventListener(IOErrorEvent.IO_ERROR, onIOErrorEvent);
		}

		public function writeByte(byte:int):void {
			if (isOpened()) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Write: {}", HexUtils.encodeByte(byte));
				}
				_socket.writeByte(byte);
				_socket.flush();
			}
		}

		public function writeBytes(bytes:ByteArray):void {
			if (isOpened()) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Write: {}", HexUtils.encodeBytes(bytes, " "));
				}
				_socket.writeBytes(bytes);
				_socket.flush();
			}
		}

		public function close():void {
			if (isOpened()) {
				_socket.close();
				onClose(null);
			}
		}

		private function isOpened():Boolean {
			return _socket !== null && _socket.connected;
		}

		private function destroy():void {
			_socket.removeEventListener(ProgressEvent.SOCKET_DATA, onSocketData);
			_socket.removeEventListener(Event.CLOSE, onClose);
			_socket.removeEventListener(IOErrorEvent.IO_ERROR, onIOErrorEvent);
			_socket = null;
			_handler = null;
		}

		private function onIOErrorEvent(event:IOErrorEvent):void {
			LogManager.getLoggerByClass(SocketChannel).warn("Catch IO error", new ErrorEventException(event));
			_handler.handleClose();
			destroy();
		}

		private function onClose(event:Event):void {
			LOGGER.trace("Close");
			_handler.handleClose();
			destroy();
		}

		private function onSocketData(event:ProgressEvent):void {
			var data:ByteArray = new ByteArray();
			_socket.readBytes(data);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Received: {}", HexUtils.encodeBytes(data, " "));
			}
			_handler.handleData(data);
		}

	}
}
