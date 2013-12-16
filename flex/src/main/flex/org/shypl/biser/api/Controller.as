package org.shypl.biser.api
{
	import flash.events.EventDispatcher;

	import org.shypl.biser.InputBuffer;
	import org.shypl.biser.OutputBuffer;
	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.logging.ILogger;
	import org.shypl.common.logging.LogManager;

	[Abstract]
	[Event(name="error", type="flash.events.ErrorEvent")]
	public class Controller extends EventDispatcher
	{
		private const _logger:ILogger = LogManager.getLogger(Controller);
		private var _connection:IConnection;
		private var _reader:MessageReceiver;
		private var _serviceResultHandlers:Vector.<ResultHandler> = new Vector.<ResultHandler>();

		public function Controller(connection:IConnection)
		{
			_connection = connection;
			_reader = new MessageReceiver(this);
			_connection.setMessageReceiver(_reader);
		}

		public function destroy():void
		{
			if (!_connection.closed) {
				_connection.close();
			}
			_connection = null;

			_reader.destroy();
			_reader = null;

			for (var i:int = 0; i < _serviceResultHandlers.length; i++) {
				_serviceResultHandlers[i].destroy();
				_serviceResultHandlers[i] = null;
			}
			_serviceResultHandlers.length = 0;
			_serviceResultHandlers = null;
		}

		internal function _send(buffer:OutputBuffer):void
		{
			_connection.sendMessage(buffer);
		}

		internal function _handleMessage(buffer:InputBuffer):void
		{
			var result:uint = buffer.readInt();

			if (result == 0) {
				_callNotifier(buffer.readInt(), buffer.readInt(), buffer);
			}
			else {
				_serviceResultHandlers[--result].handle(buffer);
				_serviceResultHandlers[result] = null;
				var len:uint = _serviceResultHandlers.length;
				result = len;
				while (result-- > 0) {
					if (_serviceResultHandlers[result] !== null) {
						break;
					}
					--len;
				}
				_serviceResultHandlers.length = len;
			}
		}

		internal function _registerServiceResultHandler(handler:ResultHandler):int
		{
			return _serviceResultHandlers.push(handler);
		}

		internal function _debug0(message:String, args:Array):void
		{
			if (_logger.debugEnabled) {
				args.unshift(message);
				_logger.debug.apply(null, args);
			}
		}

		protected function _debug(message:String, ...args):void
		{
			_debug0(message, args);
		}

		[Abstract]
		protected function _callNotifier(notifier:int, method:int, buffer:InputBuffer):void
		{
			throw new AbstractMethodException();
		}
	}
}
