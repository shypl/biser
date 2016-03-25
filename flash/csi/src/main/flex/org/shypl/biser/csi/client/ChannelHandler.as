package org.shypl.biser.csi.client {
	import flash.utils.IDataInput;

	public interface ChannelHandler {
		function handleChannelClose():void;

		function handleChannelData(data:IDataInput):void;
	}
}
