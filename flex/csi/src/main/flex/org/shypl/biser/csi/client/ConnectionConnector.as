package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.CsiException;

	internal class ConnectionConnector implements ChannelOpenHandler {
		private var _connection:Connection;
		private var _authKey:String;
		private var _connectHandler:ConnectHandler;

		public function ConnectionConnector(connection:Connection, authKey:String, connectHandler:ConnectHandler) {
			_connection = connection;
			_authKey = authKey;
			_connectHandler = connectHandler;
		}

		public function handleOpen(channel:Channel):ChannelHandler {
			var channelHandler:ChannelHandler = _connection.connect(channel, _authKey, _connectHandler);
			_connection = null;
			_authKey = null;
			_connectHandler = null;
			return channelHandler;
		}

		public function handleError(error:Error):void {
			_connection.doClose(ConnectionCloseReason.CLIENT_ERROR);
			throw new CsiException("Сan not open connection channel", error);
		}
	}
}
