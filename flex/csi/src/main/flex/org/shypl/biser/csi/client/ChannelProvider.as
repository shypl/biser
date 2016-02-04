package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.ServerEntryAddress;

	public interface ChannelProvider {
		function open(address:ServerEntryAddress, handler:ChannelOpenHandler):void;
	}
}
