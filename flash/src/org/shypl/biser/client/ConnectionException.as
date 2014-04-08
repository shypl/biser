package org.shypl.biser.client
{
	import org.shypl.common.lang.RuntimeException;

	public class ConnectionException extends RuntimeException
	{
		public function ConnectionException(message:String = null, cause:Error = null)
		{
			super(message, cause);
		}
	}
}
