package org.shypl.biser.client
{
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.events.TimerEvent;
	import flash.net.Socket;
	import flash.system.Security;
	import flash.utils.ByteArray;
	import flash.utils.Timer;

	import org.shypl.common.lang.ErrorEventException;
	import org.shypl.common.lang.IllegalArgumentException;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.ILogger;
	import org.shypl.common.logging.LogManager;
	import org.shypl.common.util.ByteUtils;
	import org.shypl.common.util.CollectionUtils;

	public class SocketChannel extends Channel
	{
		private static const STATE_MARK:uint = 0;
		private static const STATE_SID:uint = 1;
		private static const STATE_MSG_SIZE:uint = 2;
		private static const STATE_MSG_BODY:uint = 3;

		private static const logger:ILogger = LogManager.getByClass(SocketChannel);

		private var _host:String;
		private var _port:int;
		private var _timeout:uint;
		private var _connected:Boolean;
		private var _api:AbstractApi;
		private var _socket:Socket;
		private var _messageQueue:Vector.<ByteArray> = new Vector.<ByteArray>();
		private var _checkTimer:Timer;
		private var _pinged:Boolean;
		private var _state:int = STATE_MARK;
		private var _msgSize:int;
		private var _msgSizeLength:int;
		private var _msgBody:ByteArray;
		private var _msgReadSize:int;
		private var _allowReconnect:Boolean;
		private var _reconnect:Boolean;
		private var _sid:ByteArray;

		public function SocketChannel(host:String, port:int, timeout:uint)
		{
			_host = host;
			_port = port;
			_timeout = timeout * 1000;
			_checkTimer = new Timer(_timeout);
			_checkTimer.addEventListener(TimerEvent.TIMER, handleCheckTimerEvent);

			Security.loadPolicyFile("xmlsocket://" + host + ":" + port);
		}

		override internal function sendMessage(message:ByteArray):void
		{
			if (_connected) {
				writeMessage(message);
				_socket.flush();
			}
			else {
				_messageQueue.push(message);
			}
		}

		override protected function connect():void
		{
			logger.info("Connect to {}:{}", _host, _port);

			_socket = new Socket();
			_socket.addEventListener(Event.CONNECT, handleSocketEvent);
			_socket.addEventListener(Event.CLOSE, handleSocketEvent);
			_socket.addEventListener(IOErrorEvent.IO_ERROR, handleSocketErrorEvent);
			_socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSocketErrorEvent);
			_socket.addEventListener(ProgressEvent.SOCKET_DATA, handleSocketDataEvent);
			_socket.timeout = _timeout;
			_socket.connect(_host, _port);
		}

		override protected function destroy0():void
		{
			super.destroy0();
			_checkTimer.stop();
			_checkTimer.removeEventListener(TimerEvent.TIMER, handleCheckTimerEvent);
			_checkTimer = null;

			if (_msgBody) {
				_msgBody.clear();
				_msgBody = null;
			}

			if (_connected) {
				close();
			}

			_api = null;

			_sid.clear();
			_sid = null;

			CollectionUtils.clear(_messageQueue);
			_messageQueue = null;
		}

		private function catchError(error:ConnectionException):void
		{
			logger.error(error.toString());
			_api._catchChannelError(error);
			destroy();
		}

		private function close():void
		{
			logger.info("Close");

			_connected = false;

			_socket.removeEventListener(Event.CONNECT, handleSocketEvent);
			_socket.removeEventListener(Event.CLOSE, handleSocketEvent);
			_socket.removeEventListener(IOErrorEvent.IO_ERROR, handleSocketErrorEvent);
			_socket.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSocketErrorEvent);
			_socket.removeEventListener(ProgressEvent.SOCKET_DATA, handleSocketDataEvent);
			_socket.close();
			_socket = null;

			_checkTimer.stop();

			_state = STATE_MARK;
			_msgSize = 0;
			_msgSizeLength = 0;
			_msgBody = null;
			_msgReadSize = 0;
		}

		private function readMark():void
		{
			var b:uint = _socket.readUnsignedByte();
			switch (b) {
				case MARK_PING:
					break;

				case MARK_MSG_1:
					readNewMsgSize(1);
					break;

				case MARK_MSG_2:
					readNewMsgSize(2);
					break;

				case MARK_MSG_3:
					readNewMsgSize(3);
					break;

				case MARK_MSG_4:
					readNewMsgSize(4);
					break;

				case MARK_SID:
					readNewSid();
					break;

				case MARK_CLOSE:
					close();
					break;

				default:
					throw new ConnectionException("Illegal message mark " + b.toString(16) + ":" + b.toString(10));
			}
		}

		private function readMsgBody():void
		{
			var remaining:uint = _msgSize - _msgReadSize;
			if (remaining > _socket.bytesAvailable) {
				remaining = _socket.bytesAvailable;
			}

			_socket.readBytes(_msgBody, _msgReadSize, remaining);

			_msgReadSize += remaining;

			if (_msgReadSize != _msgSize) {
				_state = STATE_MSG_BODY;
				return;
			}

			_state = STATE_MARK;

			_api._receiveMessage(_msgBody);
			_msgBody.clear();
			_msgBody = null;
		}

		private function readMsgSize():void
		{
			while (_msgSizeLength > 0 && _socket.bytesAvailable > 0) {
				_msgSize |= _socket.readUnsignedByte() << (8 * --_msgSizeLength);
			}

			if (_msgSizeLength != 0) {
				_state = STATE_MSG_SIZE;
				return;
			}

			_msgBody = new ByteArray();
			_msgBody.length = _msgSize;
			_msgReadSize = 0;

			readMsgBody();
		}

		private function readNewMsgSize(i:uint):void
		{
			_msgSize = 0;
			_msgSizeLength = i;
			readMsgSize();
		}

		private function readNewSid():void
		{
			_state = STATE_SID;
			_sid = new ByteArray();
			if (_socket.bytesAvailable > 0) {
				readSid();
			}
		}

		private function readSid():void
		{
			var remaining:int = 64 - _sid.length;
			while (remaining > _socket.bytesAvailable) {
				remaining = _socket.bytesAvailable;
			}
			_socket.readBytes(_sid, _sid.length, remaining);

			if (_sid.length == 64) {
				if (logger.debugEnabled) {
					logger.debug("Received sid {}", ByteUtils.toHexString(_sid));
				}
				_state = STATE_MARK;
			}
		}

		private function reconnect():void
		{
			logger.info("Reconnect");

			_allowReconnect = false;
			_reconnect = true;
			close();
			connect();
		}

		private function writeMessage(message:ByteArray):void
		{
			if (_connected) {
				const len:uint = message.length;

				if (len <= 0xFF) {
					_socket.writeByte(MARK_MSG_1);
					_socket.writeByte(len);
				}
				else if (len <= 0xFFFF) {
					_socket.writeByte(MARK_MSG_2);
					_socket.writeByte(len >>> 8);
					_socket.writeByte(len);
				}
				else if (len <= 0xFFFFFF) {
					_socket.writeByte(MARK_MSG_3);
					_socket.writeByte(len >>> 16);
					_socket.writeByte(len >>> 8);
					_socket.writeByte(len);
				}
				else if (len <= 0x7FFFFFFF) {
					_socket.writeByte(MARK_MSG_4);
					_socket.writeInt(len);
				}
				else {
					throw new IllegalArgumentException("Maximum message length 2147483647 bytes");
				}

				_socket.writeBytes(message);
			}
			else {
				throw new IllegalStateException("Connection is not opened");
			}
		}

		private function handleSocketEvent(event:Event):void
		{
			try {
				if (event.type == Event.CONNECT) {
					logger.info("Connected");

					_socket.removeEventListener(Event.CONNECT, handleSocketEvent);

					_connected = true;
					_allowReconnect = true;

					if (_reconnect) {
						if (logger.debugEnabled) {
							logger.debug("Send sid {}", ByteUtils.toHexString(_sid));
						}
						_sid.position = 0;
						_socket.writeByte(MARK_SID);
						_socket.writeBytes(_sid);
						_socket.flush();
						_reconnect = false;
					}

					if (_messageQueue.length != 0) {
						for (var i:int = 0; i < _messageQueue.length; i++) {
							writeMessage(_messageQueue[i]);
							_messageQueue[i] = null;
						}
						_socket.flush();
						_messageQueue.length = 0;
					}

					if (_connected) {
						_checkTimer.start();
					}
				}
				else {
					logger.warn("Connection lost");
					reconnect();
				}
			}
			catch (e:ConnectionException) {
				catchError(e);
			}
			catch (e:Error) {
				catchError(new ConnectionException("Error in handle socket data", e));
			}
		}

		private function handleSocketErrorEvent(event:ErrorEvent):void
		{
			switch (event.type) {
				case SecurityErrorEvent.SECURITY_ERROR:
					catchError(new ConnectionException("Socket security error", new ErrorEventException(event)));
					break;
				case IOErrorEvent.IO_ERROR:
					catchError(new ConnectionException("Socket io error", new ErrorEventException(event)));
					break;
				default:
					catchError(new ConnectionException("Socket error", new ErrorEventException(event)));
					break;
			}
		}

		private function handleSocketDataEvent(event:ProgressEvent):void
		{
			try {
				_checkTimer.stop();
				_pinged = true;

				while (_connected && _socket.bytesAvailable != 0) {
					switch (_state) {
						case STATE_MARK:
							readMark();
							break;

						case STATE_MSG_SIZE:
							readMsgSize();
							break;

						case STATE_MSG_BODY:
							readMsgBody();
							break;

						case STATE_SID:
							readSid();
							break;
					}
				}

				if (_connected) {
					_checkTimer.start();
				}
			}
			catch (e:ConnectionException) {
				catchError(e);
			}
			catch (e:Error) {
				catchError(new ConnectionException("Error in handle socket data", e));
			}
		}

		private function handleCheckTimerEvent(event:TimerEvent):void
		{
			try {
				if (_pinged && _connected) {
					_pinged = false;
					_socket.writeByte(MARK_PING);
					_socket.flush();
				}
				else {
					logger.warn("Ping timeout expired");
					if (_allowReconnect) {
						reconnect();
					}
					else {
						throw new ConnectionException("Server did not respond for a long time, the connection is closed");
					}
				}
			}
			catch (e:ConnectionException) {
				catchError(e);
			}
			catch (e:Error) {
				catchError(new ConnectionException("Error in handle socket data", e));
			}
		}
	}
}
