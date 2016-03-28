package org.shypl.biser.csi.client.standard {
	import org.shypl.biser.csi.Address;
	import org.shypl.biser.csi.client.ChannelAcceptor;
	import org.shypl.biser.csi.client.ChannelProvider;
	import org.shypl.common.lang.IllegalArgumentException;
	import org.shypl.common.net.InetSocketAddress;

	public class StandardChannelProvider implements ChannelProvider {
		public function StandardChannelProvider() {
		}

		public function openChannel(address:Address, acceptor:ChannelAcceptor):void {
			if (address.isSocket()) {
				provideSocket(address.socket, acceptor);
			}
			else {
				throw new IllegalArgumentException();
			}
		}

		protected function provideSocket(address:InetSocketAddress, acceptor:ChannelAcceptor):void {
			new SocketChannel(address, acceptor);
		}
	}
}
