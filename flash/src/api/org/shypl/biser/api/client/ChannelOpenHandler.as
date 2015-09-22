package org.shypl.biser.api.client {
	public interface ChannelOpenHandler {
		function handleOpen(channel:Channel):ChannelHandler;

		function handleError(error:Error):void;
	}
}
