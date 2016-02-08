package org.shypl.biser.csi.client {
	import flash.utils.IDataInput;

	import org.shypl.asak.lang.AbstractMethodException;
	import org.shypl.asak.logging.Logger;

	[Abstract]
	internal class ConnectionStrategy implements ChannelHandler {
		protected var _connection:Connection;
		private var _channelHandler:ChannelHandlerNormal;

		public function ConnectionStrategy() {
		}

		[Abstract]
		public function handleData(data:IDataInput):void {
			throw new AbstractMethodException();
		}

		public function handleClose():void {
			destroy();
		}

		protected final function switchStrategy(strategy:ConnectionStrategy):void {
			_channelHandler.setStrategy(strategy);
		}

		protected final function getLogger():Logger {
			return _connection.getLogger();
		}

		protected final function isActive():Boolean {
			return _connection != null;
		}

		internal function init(connection:Connection, channelHandler:ChannelHandlerNormal):void {
			_connection = connection;
			_channelHandler = channelHandler;
		}

		internal function destroy():void {
			_connection = null;
			_channelHandler = null;
		}
	}
}
