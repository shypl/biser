package org.shypl.biser.api.client {
	import flash.utils.IDataInput;

	public interface ChannelHandler {
		function handleData(data:IDataInput):void;

		function handleClose():void;
	}
}
