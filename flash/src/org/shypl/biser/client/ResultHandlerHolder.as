package org.shypl.biser.client
{
	import org.shypl.biser.InputBuffer;
	import org.shypl.lang.AbstractMethodException;
	import org.shypl.logging.ILogger;

	[Abstract]
	public class ResultHandlerHolder
	{
		protected const _logger:ILogger = logger;

		internal function precess(message:InputBuffer):void
		{
			handle(message);
		}

		[Abstract]
		protected function handle(message:InputBuffer):void
		{
			throw new AbstractMethodException();
		}
	}
}
