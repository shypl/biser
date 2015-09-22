package org.shypl.biser.api.client {
	import flash.events.EventDispatcher;
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.api.Protocol;
	import org.shypl.biser.api.ServerEntryAddress;
	import org.shypl.common.collection.LinkedList;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.LogManager;
	import org.shypl.common.logging.Logger;
	import org.shypl.common.logging.PrefixedLoggerProxy;

	[Event(type="org.shypl.biser.api.client.ConnectionEvent", name="CONNECTION_ACTIVE")]
	[Event(type="org.shypl.biser.api.client.ConnectionEvent", name="CONNECTION_INACTIVE")]
	[Event(type="org.shypl.biser.api.client.ConnectionCloseEvent", name="CONNECTION_CLOSE")]
	public class Connection extends EventDispatcher {
		private var _connected:Boolean;
		private var _closed:Boolean;

		private var _messages:LinkedList = new LinkedList();
		private var _sendMessageEven:Boolean;
		private var _sendMessageReady:Boolean = true;
		private var _receiveMessageEven:Boolean;

		private var _channelProvider:ChannelProvider;
		private var _address:ServerEntryAddress;
		private var _gate:AbstractApiGate;
		private var _logger:Logger;

		private var _channel:Channel;
		private var _channelHandler:ChannelHandlerNormal;
		private var _sid:ByteArray;
		private var _reducer:ConnectionReducer;

		public function Connection(channelProvider:ChannelProvider, gate:AbstractApiGate, address:ServerEntryAddress, authKey:String,
			connectHandler:ConnectHandler) {
			_channelProvider = channelProvider;
			_address = address;
			_gate = gate;

			_logger = new PrefixedLoggerProxy(LogManager.getLoggerByClass(Connection), "<" + _address + "> ");

			_logger.info("Connect");

			_gate.setConnection(this);

			openChannel(new ConnectionConnector(this, authKey, connectHandler));
		}

		public function close():void {
			doClose(ConnectionCloseReason.CLOSE);
		}

		public function getLogger():Logger {
			return _logger;
		}

		internal function openChannel(handler:ChannelOpenHandler):void {
			_channelProvider.open(_address, handler);
		}

		internal function doClose(reason:ConnectionCloseReason):void {
			if (!_closed) {
				_logger.info("Close by reason {}", reason);
				_closed = true;
				if (_connected) {
					_connected = false;
					_channelHandler.destroy();
					_channel.writeByte(Protocol.CLOSE);
					_channel.close();
					_channel = null;
					_channelHandler = null;
				}
				_gate.removeConnection();

				_channelProvider = null;
				_address = null;
				_gate = null;
				_logger = null;

				stopReducer();

				dispatchEvent(new ConnectionCloseEvent(reason));
			}
		}

		private function stopReducer():Boolean {
			if (_reducer) {
				_reducer.stop();
				_reducer = null;
				return true;
			}
			return false;
		}

		internal function connect(channel:Channel, authKey:String, connectHandler:ConnectHandler):ChannelHandler {
			if (_closed) {
				_logger.warn("Connect fail, connection closed");
				doClose(ConnectionCloseReason.CLIENT_ERROR);
				connectHandler.handlerConnectFail(ConnectionCloseReason.CLOSE);
				return new ChannelHandlerEmpty();
			}

			if (_connected) {
				doClose(ConnectionCloseReason.CLIENT_ERROR);
				throw new IllegalStateException();
			}
			_connected = true;

			_logger.debug("Channel opened, authorize by key {}", authKey);

			_channel = channel;
			_channelHandler = new ChannelHandlerNormal(this);
			_channelHandler.setStrategy(new ConnectionStrategyConnect(connectHandler));

			const key:ByteArray = new ByteArray();
			key.writeUTFBytes(authKey);

			channel.writeByte(Protocol.CONNECT);
			channel.writeByte(key.length);
			channel.writeBytes(key);

			return _channelHandler;
		}

		internal function reconnect(channel:Channel):ChannelHandler {
			if (_closed) {
				_logger.warn("Reconnect fail, connection closed");
				doClose(ConnectionCloseReason.CLIENT_ERROR);
				return new ChannelHandlerEmpty();
			}

			if (_connected) {
				doClose(ConnectionCloseReason.CLIENT_ERROR);
				throw new IllegalStateException();
			}
			_connected = true;

			_logger.debug("Channel opened for reconnect");

			_channel = channel;
			_channelHandler = new ChannelHandlerNormal(this);

			_channelHandler.setStrategy(new ConnectionStrategyConnect());

			channel.writeByte(Protocol.RECONNECT);
			channel.writeBytes(_sid);

			return _channelHandler;
		}

		internal function processChannelBroken():void {
			if (_connected) {
				_connected = false;

				_channelHandler.destroy();
				_channel.close();

				_channelHandler = null;
				_channel = null;

				_reducer = new ConnectionReducer(this);

				dispatchEvent(new ConnectionEvent(ConnectionEvent.CONNECTION_INACTIVE));
			}
		}

		internal function sendByte(byte:int):void {
			if (_connected) {
				_channel.writeByte(byte);
			}
		}

		internal function sendMessage(message:ByteArray):void {
			_messages.addLast(message);
			if (_connected && _sendMessageReady) {
				sendMessage0(message);
			}
		}

		private function sendMessage0(message:ByteArray):void {
			_sendMessageReady = false;
			_sendMessageEven = !_sendMessageEven;

			var data:ByteArray = new ByteArray();
			data.writeByte(_sendMessageEven ? Protocol.MESSAGE_EVEN : Protocol.MESSAGE_ODD);
			data.writeInt(message.length);
			data.writeBytes(message);

			_channel.writeBytes(data);
		}

		internal function completeMessageSend(even:Boolean):void {
			_sendMessageReady = true;
			if (_connected) {
				if (_sendMessageEven != even) {
					sendMessage0(ByteArray(_messages.getFirst()));
				}
				else {
					if (!_messages.isEmpty()) {
						_messages.removeFirst();
						if (!_messages.isEmpty()) {
							sendMessage0(ByteArray(_messages.getFirst()));
						}
					}
				}
			}
		}

		internal function receiveMessage(even:Boolean, data:IDataInput):void {
			if (_connected) {
				_receiveMessageEven = even;
				_channel.writeByte(_receiveMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);
				_gate.processMessage(data);
			}
		}

		internal function authorize(sid:ByteArray):void {
			getLogger().info("Authorization success");
			_sid = sid;

			_channelHandler.setStrategy(new ConnectionStrategyMessaging());

			dispatchEvent(new ConnectionEvent(ConnectionEvent.CONNECTION_ACTIVE));

			if (stopReducer()) {
				_channel.writeByte(_receiveMessageEven ? Protocol.MESSAGE_EVEN_RECEIVED : Protocol.MESSAGE_ODD_RECEIVED);
			}
		}
	}
}




