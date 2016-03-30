package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;

	import org.shypl.biser.csi.CommunicationLoggingUtils;
	import org.shypl.biser.io.DataWriter;

	public class ServerService {
		private var _api:Api;
		private var _serviceId:int;
		private var _serviceName:String;
		private var _message:ByteArray;

		public function ServerService(api:Api, serviceId:int, serviceName:String) {
			_api = api;
			_serviceId = serviceId;
			_serviceName = serviceName;
		}

		protected final function _prepareMessage(methodId:int, holder:ResultHandlerHolder = null):DataWriter {
			_message = new ByteArray();
			var writer:DataWriter = new DataWriter(_message);
			writer.writeInt(_serviceId);
			writer.writeInt(methodId);
			if (holder != null) {
				writer.writeInt(_api.registerResultHandler(holder));
			}
			return writer;
		}

		protected final function _sendMessage():void {
			_api.sendMessage(_message);
			_message = null;
		}

		protected final function _log(methodName:String, ...args):void {
			CommunicationLoggingUtils.logClientCall(_api.logger, _serviceName, methodName, args);
		}
	}
}
