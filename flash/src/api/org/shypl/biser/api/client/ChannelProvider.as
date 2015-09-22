package org.shypl.biser.api.client {
	import org.shypl.biser.api.ServerEntryAddress;

	public interface ChannelProvider {
		function open(address:ServerEntryAddress, handler:ChannelOpenHandler):void;
	}
}
