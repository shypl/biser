package org.shypl.biser.csi.client {
	import flash.events.EventDispatcher;

	import org.shypl.biser.csi.Address;
	import org.shypl.biser.csi.ConnectionCloseReason;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.logging.LogManager;
	import org.shypl.common.logging.Logger;
	import org.shypl.common.logging.PrefixedLoggerProxy;

	[Event(type="org.shypl.biser.csi.client.ClientEvent", name="clientConnected")]
	[Event(type="org.shypl.biser.csi.client.ClientEvent", name="clientConnectionInterrupted")]
	[Event(type="org.shypl.biser.csi.client.ClientEvent", name="clientConnectionEstablished")]
	[Event(type="org.shypl.biser.csi.client.ClientConnectFailEvent", name="clientConnectFail")]
	[Event(type="org.shypl.biser.csi.client.ClientDisconnectedEvent", name="clientDisconnected")]
	[Event(type="org.shypl.biser.csi.client.ClientDisconnectWarningEvent", name="clientDisconnectWarning")]
	public class Client extends EventDispatcher {
		private static const LOGGER:Logger = LogManager.getLogger(Client);

		private var _channelProvider:ChannelProvider;
		private var _api:AbstractApi;
		private var _logger:PrefixedLoggerProxy;

		private var _connecting:Boolean;
		private var _connected:Boolean;
		private var _connection:Connection;

		public function Client(channelProvider:ChannelProvider, api:AbstractApi) {
			_channelProvider = channelProvider;
			_api = api;

			_logger = new PrefixedLoggerProxy(LOGGER, "[" + _api.name + "] ");
		}

		internal function get channelProvider():ChannelProvider {
			return _channelProvider;
		}

		internal function get api():AbstractApi {
			return _api;
		}

		public function isConnecting():Boolean {
			return _connecting;
		}

		public function isConnected():Boolean {
			return _connected;
		}

		public function connect(address:Address, authorizationKey:String):void {
			if (isConnecting() || isConnected()) {
				throw new IllegalStateException();
			}

			_logger.info("Connecting to {}", address);
			_connecting = true;
			_connection = new Connection(this, address, authorizationKey);
			_api.setConnection(_connection);
		}

		public function disconnect():void {
			if (!_connecting && !_connected) {
				throw new IllegalStateException();
			}
			_connecting = false;

			_logger.info("Disconnecting");
			_connection.close(ConnectionCloseReason.NONE);
		}

		internal function processConnected():void {
			_logger.info("Connected");
			_connecting = false;
			_connected = true;
			dispatchEvent(new ClientEvent(ClientEvent.CLIENT_CONNECTED));
		}

		internal function processDisconnected(reason:ConnectionCloseReason):void {
			_logger.info("Disconnected by reason {}", reason);

			_connecting = false;
			_connected = false;
			_connection = null;
			_api.setConnection(null);
			dispatchEvent(new ClientDisconnectedEvent(reason));
		}

		internal function processConnectFail(reason:Error):void {
			_logger.info("Connect failed by reason {}", reason);
			_connecting = false;
			_connected = false;
			dispatchEvent(new ClientConnectFailEvent(reason));
		}

		internal function processConnectionInterrupted():void {
			_logger.info("Connection is interrupted");
			dispatchEvent(new ClientEvent(ClientEvent.CLIENT_CONNECTION_INTERRUPTED));
		}

		internal function processConnectionEstablished():void {
			_logger.info("Connection is established");
			dispatchEvent(new ClientEvent(ClientEvent.CLIENT_CONNECTION_ESTABLISHED));
		}

		internal function processDisconnectWarning(timeout:int):void {
			_logger.info("warning to disconnect after {} seconds", timeout);
			dispatchEvent(new ClientDisconnectWarningEvent(timeout));
		}
	}
}
