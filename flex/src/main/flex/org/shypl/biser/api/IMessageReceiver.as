package org.shypl.biser.api
{
	import org.shypl.biser.InputBuffer;

	public interface IMessageReceiver
	{
		function receiveMessage(message:InputBuffer):void;
	}
}
