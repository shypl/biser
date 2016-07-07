package org.shypl.biser.csi.client {
	public interface ChannelAcceptor {
		function acceptChannel(channel:Channel):ChannelHandler;

		function failOpenChannel(error:Error):void;
	}
}
