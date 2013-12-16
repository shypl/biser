package org.shypl.biser.api
{
	import org.shypl.biser.InputBuffer;

	internal class ResultHandler
	{
		private var _service:Service;
		private var _action:int;
		private var _handler:Object;

		public function ResultHandler(service:Service, action:int, handler:Object)
		{
			_service = service;
			_action = action;
			_handler = handler;
		}

		public function handle(buffer:InputBuffer):void
		{
			_service._handleResult0(_action, _handler, buffer);
			destroy();
		}

		public function destroy():void
		{
			_service = null;
			_handler = null;
		}
	}
}
