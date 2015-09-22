package org.shypl.biser.api.client {
	import org.shypl.biser.io.BiserReader;
	import org.shypl.common.lang.AbstractMethodException;

	public class Service {
		private var _gate:AbstractApiGate;

		internal function _setGate(gate:AbstractApiGate):void {
			_gate = gate;
		}

		internal function _executeAction0(id:int, reader:BiserReader):void {
			_executeAction(id, reader);
		}

		[Abstract]
		protected function _executeAction(id:int, reader:BiserReader):void {
			throw new AbstractMethodException();
		}

		protected final function _log(message:String, ...args):void {
			_gate.log(message, args);
		}
	}
}
