package org.shypl.biser.client
{
	import flash.utils.ByteArray;

	import org.shypl.common.lang.AbstractMethodException;
	import org.shypl.common.lang.IllegalStateException;
	import org.shypl.common.util.Destroyable;

	[Abstract]
	public class Channel extends Destroyable
	{
		protected static const MARK_CLOSE:int = 0x00;
		protected static const MARK_PING:int = 0xFF;
		protected static const MARK_SID:int = 0xFE;
		protected static const MARK_MSG_1:int = 0x01;
		protected static const MARK_MSG_2:int = 0x02;
		protected static const MARK_MSG_3:int = 0x03;
		protected static const MARK_MSG_4:int = 0x04;

		private var _api:AbstractApi;

		internal function bind(api:AbstractApi):void
		{
			if (_api !== null) {
				throw new IllegalStateException("This channel is already binding to APIs");
			}
			_api = api;
			connect();
		}

		[Abstract]
		internal function sendMessage(bytes:ByteArray):void
		{
			throw new AbstractMethodException();
		}

		override protected function doDestroy():void
		{
			super.doDestroy();
			_api = null;
		}

		[Abstract]
		protected function connect():void
		{
			throw new AbstractMethodException();
		}
	}
}
