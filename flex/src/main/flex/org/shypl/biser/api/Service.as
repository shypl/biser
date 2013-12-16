package org.shypl.biser.api
{
	import org.shypl.biser.InputBuffer;
	import org.shypl.biser.OutputBuffer;

	[Abstract]
	public class Service
	{
		protected const _buffer:OutputBuffer = new OutputBuffer();
		private var _controller:Controller;
		private var _headerLength:int;

		public function Service(id:int, controller:Controller)
		{
			_controller = controller;
			_buffer.writeInt(id);
			_headerLength = _buffer.size;
		}

		internal function __handleResult(action:int, handler:Object, buffer:InputBuffer):void
		{
			_handleResult(action, handler, buffer);
		}

		[Abstract]
		protected function _handleResult(action:int, handler:Object, buffer:InputBuffer):void
		{
			throw new Error();
		}

		protected final function _send():void
		{
			_controller.__send(_buffer);
			_buffer.clearTo(_headerLength);
		}

		protected final function _writeHandler(action:int, handler:Object):void
		{
			_buffer.writeInt(action);
			_buffer.writeInt(_controller.__registerServiceResultHandler(new ResultHandler(this, action, handler)));
		}

		protected final function _debug(message:String, ...args):void
		{
			_controller.__debug(message, args);
		}
	}
}
