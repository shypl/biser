package org.shypl.biser.csi.client {
	import org.shypl.biser.csi.Address;

	public interface ChannelProvider {
		function provide(address:Address, acceptor:ChannelAcceptor):void;
	}
}
