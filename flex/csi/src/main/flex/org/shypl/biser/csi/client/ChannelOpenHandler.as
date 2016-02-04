package org.shypl.biser.csi.client {
	public interface ChannelOpenHandler {
		function handleOpen(channel:Channel):ChannelHandler;

		function handleError(error:Error):void;
	}
}
