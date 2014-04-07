package org.shypl.biser
{
	[Abstract]
	public class BiserEntity
	{
		internal function get __eid():int
		{
			return _eid();
		}

		public final function toString():String
		{
			const r:Represent = new Represent();
			represent(r);
			return r.toString();
		}

		public function encode(buffer:OutputBuffer):void
		{
		}

		protected function _eid():int
		{
			return 0;
		}

		protected function represent(r:Represent):void
		{
		}
	}
}
