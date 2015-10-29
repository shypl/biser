package org.shypl.biser.api.server;

import org.shypl.biser.api.Protocol;
import org.shypl.biser.api.ProtocolException;

class ConnectionStrategyReception extends ConnectionStrategy {
	@Override
	public void handleData() throws ProtocolException {
		final byte flag = connection.readSync();
		switch (flag) {
			case Protocol.CROSS_DOMAIN_POLICY:
				direct(new ConnectionStrategyCrossDomainPolicy(), "Reception: Directed to CrossDomainPolicy");
				break;
			case Protocol.CONNECT:
				direct(new ConnectionStrategyConnect(), "Reception: Directed to Connect");
				break;
			case Protocol.RECONNECT:
				direct(new ConnectionStrategyReconnect(), "Reception: Directed to Reconnect");
				break;
			default:
				throw new ProtocolException("Reception: Invalid flag " + String.format("%02x", flag) + "(" + ((char)flag) + ")");
		}
	}

	@Override
	public void handleClose(boolean broken) {}

	private void direct(ConnectionStrategy strategy, String logMessage) {
		connection.getLogger().trace(logMessage);
		connection.setStrategy(strategy);
	}
}
