package org.shypl.biser.client
{
	import org.shypl.biser.OutputBuffer;
	import org.shypl.logging.ILogger;

	[Abstract]
	public class Service
	{
		protected const _logger:ILogger = logger;
		private var _id:int;
		private var _api:AbstractApi;

		public function Service(id:int, api:AbstractApi)
		{
			_id = id;
			_api = api;
		}

		protected final function _createMessage(method:int):OutputBuffer
		{
			const message:OutputBuffer = new OutputBuffer();
			message.writeInt(_id);
			message.writeInt(method);
			return message;
		}

		protected final function _createMessageWithResult(method:int, holder:ResultHandlerHolder):OutputBuffer
		{
			const message:OutputBuffer = _createMessage(method);
			message.writeInt(_api._registerResultHandlerHolder(holder));
			return message;
		}

		protected final function _sendMessage(message:OutputBuffer):void
		{
			_api._sendMessage(message.getBytes());
			message.clear();
		}
	}
}
