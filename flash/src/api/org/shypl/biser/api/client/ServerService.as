package org.shypl.biser.api.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.io.BiserWriter;
	import org.shypl.biser.io.StreamWriter;

	[Abstract]
	public class ServerService {
		private var _id:int;
		private var _gate:AbstractApiGate;
		private var _data:ByteArray;

		public function ServerService(id:int, gate:AbstractApiGate) {
			_id = id;
			_gate = gate;
		}

		protected final function _log(message:String, ...args):void {
			_gate._log(message, args);
		}

		protected final function _prepareMessage(actionId:int, holder:ResultHandlerHolder = null):BiserWriter {
			_data = new ByteArray();
			var writer:StreamWriter = new StreamWriter(_data);
			writer.writeInt(_id);
			writer.writeInt(actionId);
			if (holder != null) {
				writer.writeInt(_gate.registerResultHandler(holder));
			}
			return writer;
		}

		protected final function _sendMessage():void {
			_gate.sendMessage(_data);
			_data = null;
		}
	}
}
