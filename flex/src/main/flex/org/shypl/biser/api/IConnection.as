package org.shypl.biser.api
{
	import org.shypl.biser.OutputBuffer;

	public interface IConnection
	{
		function get closed():Boolean;

		function get interrupted():Boolean;

		function sendMessage(message:OutputBuffer):void;

		function setMessageReceiver(receiver:IMessageReceiver):void;

		function close():void;
	}
}
