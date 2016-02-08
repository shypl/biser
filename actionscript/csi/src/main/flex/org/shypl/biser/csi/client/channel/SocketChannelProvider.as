package org.shypl.biser.csi.client.channel {
	import org.shypl.biser.csi.ServerEntryAddress;
	import org.shypl.biser.csi.client.ChannelOpenHandler;
	import org.shypl.biser.csi.client.ChannelProvider;

	public class SocketChannelProvider implements ChannelProvider {
		public function open(address:ServerEntryAddress, handler:ChannelOpenHandler):void {
			new SocketChannelOpener(address, handler);
		}
	}
}
