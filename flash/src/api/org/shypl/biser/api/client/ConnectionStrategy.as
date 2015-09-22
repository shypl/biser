package org.shypl.biser.api.client {
	import flash.utils.IDataInput;

	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.logging.Logger;

	[Abstract]
	internal class ConnectionStrategy implements ChannelHandler {
		protected var _connection:Connection;
		private var _channelHandler:ChannelHandlerNormal;

		[Abstract]
		public function handleData(data:IDataInput):void {
			throw new AbstractMethodException();
		}

		public function handleClose():void {
			destroy();
		}

		internal function init(connection:Connection, channelHandler:ChannelHandlerNormal):void {
			_connection = connection;
			_channelHandler = channelHandler;
		}

		internal function destroy():void {
			_connection = null;
			_channelHandler = null;
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
	}
}
