package org.shypl.biser.csi.client {
	import flash.utils.IDataInput;

	public interface ChannelHandler {
		function handleData(data:IDataInput):void;

		function handleClose():void;
	}
}
