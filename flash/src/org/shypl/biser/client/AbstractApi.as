package org.shypl.biser.client
{
	import flash.utils.ByteArray;

	import org.shypl.biser.InputBuffer;
	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.logging.ILogger;
	import org.shypl.common.util.CollectionUtils;
	import org.shypl.common.util.Destroyable;
	import org.shypl.common.util.IErrorHandler;

	[Abstract]
	public class AbstractApi extends Destroyable
	{
		protected const _logger:ILogger = logger;
		private var _resultHandlers:Vector.<ResultHandlerHolder> = new Vector.<ResultHandlerHolder>();
		private var _channel:Channel;
		private var _errorHandler:IErrorHandler;

		public function AbstractApi(errorHandler:IErrorHandler, channel:Channel)
		{
			_errorHandler = errorHandler;
			_channel = channel;
			_channel.bind(this);
		}

		internal function _receiveMessage(bytes:ByteArray):void
		{
			if (destroyed) {
				return;
			}

			const message:InputBuffer = new InputBuffer(bytes);
			var resultId:uint = message.readInt();

			if (resultId == 0) {
				_route(message.readInt(), message.readInt(), message);
			}
			else {
				_resultHandlers[--resultId].precess(message);
				_resultHandlers[resultId] = null;
				var len:uint = _resultHandlers.length;
				resultId = len;
				while (resultId-- > 0) {
					if (_resultHandlers[resultId] !== null) {
						break;
					}
					--len;
				}
				_resultHandlers.length = len;
			}
		}

		internal function _registerResultHandlerHolder(handler:ResultHandlerHolder):int
		{
			return _resultHandlers.push(handler);
		}

		internal function _sendMessage(bytes:ByteArray):void
		{
			_channel.sendMessage(bytes);
		}

		internal function _catchError(e:Error):void
		{
			_errorHandler.handleError(e);
			destroy();
		}

		override protected function doDestroy():void
		{
			super.doDestroy();
			_channel.destroy();
			_channel = null;
			CollectionUtils.clear(_resultHandlers);
			_resultHandlers = null;
		}

		[Abstract]
		protected function _route(service:int, method:int, buffer:InputBuffer):void
		{
			throw new AbstractMethodException();
		}
	}
}
