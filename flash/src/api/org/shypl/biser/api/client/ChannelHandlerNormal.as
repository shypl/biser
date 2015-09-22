package org.shypl.biser.api.client {
	import flash.utils.IDataInput;

	import org.shypl.common.lang.IllegalStateException;

	internal class ChannelHandlerNormal implements ChannelHandler {
		private var _connection:Connection;
		private var _strategy:ConnectionStrategy;
		private var _opened:Boolean = true;

		public function ChannelHandlerNormal(connection:Connection) {
			_connection = connection;
		}

		public function handleData(data:IDataInput):void {
			do {
				_strategy.handleData(data);
			}
			while (_opened && data.bytesAvailable > 0);
		}

		public function handleClose():void {
			_opened = false;
			if (_strategy) {
				_strategy.handleClose();
			}
			destroy();
		}

		internal function destroy():void {
			_opened = false;
			if (_strategy) {
				_strategy.destroy();
			}
			_strategy = null;
			_connection = null;
		}

		internal function setStrategy(strategy:ConnectionStrategy):void {
			if (!_opened) {
				throw new IllegalStateException();
			}
			if (_strategy) {
				_strategy.destroy();
			}
			_strategy = strategy;
			_strategy.init(_connection, this);
		}
	}
}
