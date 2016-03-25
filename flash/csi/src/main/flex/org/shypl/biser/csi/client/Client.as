package org.shypl.biser.csi.client {
	import flash.events.EventDispatcher;

	import org.shypl.biser.csi.Address;
	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.LogManager;
	import org.shypl.common.logging.Logger;
	import org.shypl.common.logging.PrefixedLoggerProxy;

	[Event(type="org.shypl.biser.csi.client.ClientEvent", name="connectionEstablished")]
	[Event(type="org.shypl.biser.csi.client.ClientEvent", name="connectionInterrupted")]
	[Event(type="org.shypl.biser.csi.client.ClientDisconnectedEvent", name="disconnected")]
	public class Client extends EventDispatcher {
		private static const LOGGER:Logger = LogManager.getLogger(Client);

		private var _channelProvider:ChannelProvider;
		private var _api:Api;
		private var _logger:PrefixedLoggerProxy;

		private var _connected:Boolean;
		private var _connection:Connection;

		public function Client(channelProvider:ChannelProvider, api:Api) {
			_channelProvider = channelProvider;
			_api = api;

			_logger = new PrefixedLoggerProxy(LOGGER, "<" + _api.name + "> ");
		}

		internal function get channelProvider():ChannelProvider {
			return _channelProvider;
		}

		internal function get logger():PrefixedLoggerProxy {
			return _logger;
		}

		internal function get api():Api {
			return _api;
		}

		public function connect(address:Address, authorizationKey:String):void {
			if (_connected) {
				throw new IllegalStateException("Client already connected");
			}
			_logger.info("Connecting", address);
			_connected = true;
			_connection = new Connection(this, address, authorizationKey);
			_api.setConnection(_connection);
		}

		public function disconnect():void {
			if (!_connected) {
				throw new IllegalStateException("Client is no connected");
			}
			_logger.info("Disconnecting");
			_connection.close(ConnectionCloseReason.NONE);
			_connection = null;
			_api.setConnection(null);
		}

		internal function handleConnectionEstablished():void {
			_logger.info("Connection established");
			dispatchEvent(new ClientEvent(ClientEvent.CONNECTION_ESTABLISHED));
		}

		internal function handleConnectionInterrupted():void {
			_logger.info("Connection interrupted");
			dispatchEvent(new ClientEvent(ClientEvent.CONNECTION_INTERRUPTED));
		}

		internal function handleConnectionClosed(reason:ConnectionCloseReason):void {
			_logger.info("Disconnected");
			_connected = false;
			dispatchEvent(new ClientDisconnectedEvent(reason));
		}
	}
}
