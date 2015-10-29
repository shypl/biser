package org.shypl.biser.api.client.channel {
	import org.shypl.biser.api.ServerEntryAddress;
	import org.shypl.biser.api.client.ChannelOpenHandler;
	import org.shypl.biser.api.client.ChannelProvider;

	public class SocketChannelProvider implements ChannelProvider {
		public function open(address:ServerEntryAddress, handler:ChannelOpenHandler):void {
			new SocketChannelOpener(address, handler);
		}
	}
}
