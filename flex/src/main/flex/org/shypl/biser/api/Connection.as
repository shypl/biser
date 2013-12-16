package org.shypl.biser.api
{
	import flash.errors.IllegalOperationError;
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

	import org.shypl.biser.InputBuffer;
	import org.shypl.biser.OutputBuffer;
	import org.shypl.common.lang.EventException;
	import org.shypl.common.lang.OutOfBoundsException;
	import org.shypl.common.util.IErrorHandler;

	public class Connection implements IConnection
	{
		private static const MARK_CLOSE:int = 0x00;
		private static const MARK_CHECK:int = 0xFF;
		private static const MARK_MSG_1:int = 0x01;
		private static const MARK_MSG_2:int = 0x02;
		private static const MARK_MSG_3:int = 0x03;
		private static const MARK_MSG_4:int = 0x04;
		private static const STATE_MARK:uint = 0;
		private static const STATE_MSG_SIZE:uint = 1;
		private static const STATE_MSG_BODY:uint = 2;
		private var _errorHandler:IErrorHandler;
		private var _host:String;
		private var _port:int;
		private var _checkInterval:int;
		private var _state:uint = STATE_MARK;
		private var _socket:Socket;
		private var _sendingQueue:Vector.<ByteArray>;
		private var _opened:Boolean;
		private var _closed:Boolean;
		private var _interrupted:Boolean;
		private var _msgSizeLength:uint;
		private var _msgSize:uint;
		private var _msgBody:ByteArray;
		private var _msgReadSize:int;
		private var _messageReceiver:IMessageReceiver;
		private var _checkTimer:Timer;
		private var _checked:Boolean;
		private var _connectTimeout:int;

		public function Connection(errorHandler:IErrorHandler, host:String, port:int, checkInterval:int, connectTimeout:int = 20)
		{
			_errorHandler = errorHandler;
			_host = host;
			_port = port;
			_checkInterval = checkInterval * 1000;
			_connectTimeout = connectTimeout * 1000;

			Security.loadPolicyFile("xmlsocket://" + host + ":" + port);
		}

		public function get closed():Boolean
		{
			return _closed;
		}

		public function get interrupted():Boolean
		{
			return _interrupted;
		}

		public function sendMessage(message:OutputBuffer):void
		{
			const bytes:ByteArray = message.bytes;

			if (_opened) {
				writeMessage(bytes);
				_socket.flush();
			}
			else {
				open();
				_sendingQueue.push(bytes);
			}
		}

		public function setMessageReceiver(receiver:IMessageReceiver):void
		{
			_messageReceiver = receiver;
		}

		public function close():void
		{
			if (_closed) {
				throw new IllegalOperationError("Connection already is closed");
			}

			doClose(null);
		}

		private function writeMessage(bytes:ByteArray):void
		{
			var len:uint = bytes.length;

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
				throw new OutOfBoundsException("Maximum data message length 2147483647 bytes");
			}

			_socket.writeBytes(bytes);
		}

		private function doClose(error:Error):void
		{
			if (_opened) {
				_interrupted = true;
			}

			_closed = true;
			_opened = false;

			if (_socket) {
				_socket.removeEventListener(Event.CONNECT, handleSocketConnectEvent);
				_socket.removeEventListener(Event.CLOSE, handleSocketCloseEvent);
				_socket.removeEventListener(ProgressEvent.SOCKET_DATA, handleSocketDataEvent);
				_socket.removeEventListener(IOErrorEvent.IO_ERROR, handleSocketErrorEvent);
				_socket.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSocketErrorEvent);

				if (_socket.connected) {
					_socket.writeByte(MARK_CLOSE);
					_socket.flush();
				}
				_socket.close();

				_socket = null;
			}

			if (_checkTimer) {
				_checkTimer.stop();
				_checkTimer.removeEventListener(TimerEvent.TIMER, handleCheckTimerEvent);
				_checkTimer = null;
			}

			_messageReceiver = null;
			_host = null;
			_msgBody = null;
			_sendingQueue = null;

			if (_errorHandler && error) {
				_errorHandler.handleError(error);
			}

			_errorHandler = null;
		}

		private function open():void
		{
			if (_closed) {
				throw new IllegalOperationError("Connection is closed");
			}

			if (!_socket) {
				_socket = new Socket();

				_socket.addEventListener(Event.CONNECT, handleSocketConnectEvent);
				_socket.addEventListener(Event.CLOSE, handleSocketCloseEvent);
				_socket.addEventListener(ProgressEvent.SOCKET_DATA, handleSocketDataEvent);
				_socket.addEventListener(IOErrorEvent.IO_ERROR, handleSocketErrorEvent);
				_socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSocketErrorEvent);

				_socket.timeout = _connectTimeout;
				_socket.connect(_host, _port);

				_sendingQueue = new Vector.<ByteArray>();
			}
		}

		private function readMark():void
		{
			var b:uint = _socket.readUnsignedByte();
			switch (b) {
				case MARK_CHECK:
					_checked = true;
					break;

				case MARK_CLOSE:
					close();
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
					readNewMsgSize(3);
					break;

				default:
					doClose(new ConnectionException("Illegal message mark " + b.toString(16) + ":" + b.toString(10)));
					break;
			}
		}

		private function readNewMsgSize(i:uint):void
		{
			_msgSize = 0;
			_msgSizeLength = i;
			readMsgSize();
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

		private function readMsgBody():void
		{
			var length:uint = _msgSize - _msgReadSize;
			if (length > _socket.bytesAvailable) {
				length = _socket.bytesAvailable;
			}

			_socket.readBytes(_msgBody, _msgReadSize, length);

			_msgReadSize += length;

			if (_msgReadSize != _msgSize) {
				_state = STATE_MSG_BODY;
				return;
			}

			_state = STATE_MARK;

			_messageReceiver.receiveMessage(new InputBuffer(_msgBody));
		}

		private function handleSocketDataEvent(event:ProgressEvent):void
		{
			while (_socket.bytesAvailable > 0) {
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
				}

				if (_closed) {
					return;
				}
			}

			if (_checkTimer) {
				_checkTimer.reset();
				_checkTimer.start();
			}
		}

		private function handleSocketErrorEvent(event:ErrorEvent):void
		{
			const cause:EventException = new EventException(event);

			switch (event.type) {
				case SecurityErrorEvent.SECURITY_ERROR:
					doClose(new ConnectionException("Socket security error", cause));
					break;
				case IOErrorEvent.IO_ERROR:
					doClose(new ConnectionException("Socket io error", cause));
					break;
				default:
					doClose(new ConnectionException("Socket error", cause));
					break;
			}
		}

		private function handleSocketCloseEvent(event:Event):void
		{
			doClose(new ConnectionException("Connection has been lost", new EventException(event)));
		}

		private function handleSocketConnectEvent(event:Event):void
		{
			_opened = true;

			_socket.writeByte(MARK_CHECK);
			_socket.flush();

			if (_checkInterval > 0) {
				_checked = true;
				_checkTimer = new Timer(_checkInterval);
				_checkTimer.addEventListener(TimerEvent.TIMER, handleCheckTimerEvent);
				_checkTimer.start();
			}

			if (_sendingQueue.length != 0) {
				for (var i:int = 0; i < _sendingQueue.length; i++) {
					writeMessage(_sendingQueue[i]);
					_sendingQueue[i] = null;
				}
				_socket.flush();
				_sendingQueue.length = 0;
			}
			_sendingQueue = null;
		}

		private function handleCheckTimerEvent(event:TimerEvent):void
		{
			if (_checked && _opened && _socket.connected) {
				_checked = false;
				_socket.writeByte(MARK_CHECK);
				_socket.flush();
			}
			else {
				doClose(new ConnectionException("Server did not respond for a long time, the connection is closed"));
			}
		}
	}
}
