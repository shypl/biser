package org.shypl.biser.api
{
	import org.shypl.biser.InputBuffer;

	internal class MessageReceiver implements IMessageReceiver
	{
		private var _controller:Controller;

		public function MessageReceiver(controller:Controller)
		{
			_controller = controller;
		}

		public function receiveMessage(buffer:InputBuffer):void
		{
			_controller._handleMessage(buffer);
		}

		public function destroy():void
		{
			_controller = null;
		}
	}
}
