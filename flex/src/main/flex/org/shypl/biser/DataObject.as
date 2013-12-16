package org.shypl.biser
{
	import flash.utils.ByteArray;

	[Abstract]
	public class DataObject
	{
		public function decodeBytes(bytes:ByteArray):void
		{
			decode(new InputBuffer(bytes));
		}

		[Abstract]
		public function encode(b:OutputBuffer):void
		{
			throw new Error();
		}

		[Abstract]
		public function decode(b:InputBuffer):void
		{
			throw new Error();
		}

		public function toString():String
		{
			return Utils.representObject(this);
		}
	}
}
