package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.csi.Address;
	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.common.collection.LinkedList;
	import org.shypl.common.lang.IllegalArgumentException;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.LogManager;
	import org.shypl.common.logging.Logger;
	import org.shypl.common.logging.PrefixedLoggerProxy;
	import org.shypl.common.util.HexUtils;
	import org.shypl.common.util.callDelayed;

	internal class Connection implements ChannelHandler, ChannelAcceptor {
		private static const LOGGER:Logger = LogManager.getLogger(Connection);

		private var _client:Client;
		private var _address:Address;
		private var _logger:Logger;

		private var _opened:Boolean;
		private var _alive:Boolean;
		private var _messaging:Boolean;

		private var _processor:ConnectionProcessor;
		private var _channel:Channel;

		private var _closeReason:ConnectionCloseReason;

		private var _sid:ByteArray;
		private var _activityTimeout:int;
		private var _recoveryTimeout:int;

		private var _inputMessageEven:Boolean;
		private var _outputMessageEven:Boolean;
		private var _outputMessageSendAvailable:Boolean = true;
		private var _outputMessages:LinkedList = new LinkedList();
		private var _outputMessageBuffer:ByteArray = new ByteArray();

		public function Connection(client:Client, address:Address, authorizationKey:String) {
			_client = client;
			_address = address;
			_logger = new PrefixedLoggerProxy(LOGGER, "[" + client.api.name + "] ");

			_opened = true;
			_alive = false;
			_messaging = false;

			setProcessor(new ConnectionProcessorAuthorization(authorizationKey));
			openChannel();
		}

		internal function get client():Client {
			return _client;
		}

		internal function get opened():Boolean {
			return _opened;
		}

		internal function get logger():Logger {
			return _logger;
		}

		internal function get sid():ByteArray {
			return _sid;
		}

		internal function get activityTimeout():int {
			return _activityTimeout;
		}

		internal function get recoveryTimeout():int {
			return _recoveryTimeout;
		}

		public function close(reason:ConnectionCloseReason):void {
			if (_opened) {
				_logger.debug("Close by reason {} (alive: {})", reason, _alive);

				_opened = false;
				_closeReason = reason;

				if (_alive) {
					_channel.close();
				}
				else {
					callDelayed(handleChannelClose);
				}
			}
		}

		public function acceptChannel(channel:Channel):ChannelHandler {
			if (_opened) {
				_logger.debug("Channel opened");

				_alive = true;
				_channel = channel;
				_processor.processAccept();
				return this;
			}

			_logger.debug("Channel opened on closed connection");

			channel.close();
			return null;
		}

		public function failOpenChannel(error:Error):void {
			_logger.debug("Open channel failed ({})", error);
			callDelayed(_client.processConnectFail, error);
		}

		public function handleChannelClose():void {
			_logger.debug("Channel closed (alive: {})", _alive);


			_messaging = false;
			_alive = false;
			_channel = null;
			if (_opened) {
				callDelayed(_client.processConnectionInterrupted);
				_processor.processClose();
			}
			else {
				_processor.destroy();
				callDelayed(_client.processDisconnected, _closeReason);
				free();
			}
		}

		public function handleChannelData(data:IDataInput):void {
			_logger.trace("<< {}", data);

			while (_opened && data.bytesAvailable > 0) {
				_processor.processData(data);
			}
		}

		public function handleChannelError(error:Error):void {
			_logger.warn("Channel error {} ", error);
		}

		internal function setProcessor(processor:ConnectionProcessor):void {
			if (_processor != null) {
				_processor.destroy();
			}
			_processor = processor;
			_processor.init(this);
		}

		internal function openChannel():void {
			_logger.debug("Open channel");
			_client.channelProvider.openChannel(_address, this);
		}

		internal function interrupt():void {
			if (_alive) {
				_channel.close();
			}
			else {
				callDelayed(handleChannelClose);
			}
		}

		internal function beginSession(sid:ByteArray, activityTimeout:int, recoveryTimeout:int):void {


			_logger.debug("Begin session (sid: {}, activityTimeout: {}, recoveryTimeout: {})", sid, activityTimeout, recoveryTimeout);

			_sid = sid;
			_activityTimeout = activityTimeout;
			_recoveryTimeout = recoveryTimeout;

			callDelayed(beginSessionDelayed);
		}

		internal function recoverSession():void {
			_logger.debug("Recover session");

			writeByteToChannel(_inputMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);

			callDelayed(recoverSessionDelayed);
		}

		internal function sendByte(byte:int):void {
			if (_alive) {
				writeByteToChannel(byte);
			}
			else {
				_logger.error("Can't send data on interrupted connection");
			}
		}

		internal function sendBytes(data:ByteArray):void {
			if (_alive) {
				writeBytesToChannel(data);
			}
			else {
				_logger.error("Can't send data on interrupted connection");
			}
		}

		internal function sendMessage(message:ByteArray):void {
			if (_opened) {
				_outputMessages.addLast(message);
				if (_messaging && _outputMessageSendAvailable) {
					sendNextMessage();
				}
			}
			else {
				logger.warn("Fail send message on closed connection");
			}
		}

		internal function receiveMessage(even:Boolean, message:IDataInput):void {
			if (_inputMessageEven == even) {
				logger.error("Violation of the Message Queuing in receiveMessage()");
			}

			if (message.bytesAvailable == 0) {
				throw new IllegalArgumentException("Received message is empty");
			}

			_inputMessageEven = even;
			writeByteToChannel(_inputMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);

			_client.api.processIncomingMessage(message);
		}

		internal function processMessageReceived(even:Boolean):void {
			_outputMessageSendAvailable = true;
			if (_messaging) {
				if (_outputMessageEven != even) {
					sendNextMessage();
				}
				else {
					if (_outputMessages.isEmpty()) {
						logger.error("Violation of the Message Queuing in processMessageReceived()");
					}
					else {
						_outputMessages.removeFirst();
						sendNextMessageIfExists();
					}
				}
			}
		}

		private function beginSessionDelayed():void {
			_messaging = true;
			_client.processConnected();
			if (_messaging && _outputMessageSendAvailable) {
				sendNextMessageIfExists();
			}
		}

		private function recoverSessionDelayed():void {
			_messaging = true;
			_client.processConnectionEstablished();
		}

		private function sendNextMessageIfExists():void {
			if (!_outputMessages.isEmpty()) {
				sendMessage0(ByteArray(_outputMessages.getFirst()));
			}
		}

		private function sendNextMessage():void {
			sendMessage0(ByteArray(_outputMessages.getFirst()));
		}

		private function sendMessage0(message:ByteArray):void {
			if (!_outputMessageSendAvailable) {
				throw new IllegalStateException("Send message is not available");
			}
			if (message.length == 0) {
				throw new IllegalArgumentException("Outgoing message is empty");
			}

			_outputMessageSendAvailable = false;
			_outputMessageEven = !_outputMessageEven;

			_outputMessageBuffer.writeByte(_outputMessageEven ? Protocol.MESSAGE_EVEN : Protocol.MESSAGE_ODD);
			_outputMessageBuffer.writeInt(message.length);
			_outputMessageBuffer.writeBytes(message);
			message.clear();

			sendBytes(_outputMessageBuffer);

			_outputMessageBuffer.clear();
		}

		private function writeByteToChannel(byte:int):void {
			if (_logger.isTraceEnabled()) {
				_logger.trace(">> {}", "[" + HexUtils.encodeByte(byte) + "]");
			}
			_channel.writeByte(byte);
		}

		private function writeBytesToChannel(bytes:ByteArray):void {
			if (_logger.isTraceEnabled()) {
				_logger.trace(">> {}", bytes);
			}
			_channel.writeBytes(bytes);
		}

		private function free():void {
			_client = null;
			_address = null;
			_logger = null;
			_processor = null;
			_channel = null;
			_closeReason = null;
			_sid = null;
		}
	}
}

