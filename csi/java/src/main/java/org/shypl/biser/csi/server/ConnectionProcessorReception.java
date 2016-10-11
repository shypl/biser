package org.shypl.biser.csi.server;

import org.shypl.biser.csi.Protocol;
import org.shypl.biser.csi.ProtocolException;

class ConnectionProcessorReception extends ConnectionProcessor {
	@Override
	public void processData() throws ProtocolException {
		final byte flag = connection.read();
		switch (flag) {
			case Protocol.CROSS_DOMAIN_POLICY:
				connection.getLogger().debug("Reception: Switch to CrossDomainPolicy");
				switchProcessor(new ConnectionProcessorCrossDomainPolicy());
				break;
			case Protocol.AUTHORIZATION:
				connection.getLogger().debug("Reception: Switch to Authorization");
				switchProcessor(new ConnectionProcessorAuthorization());
				break;
			case Protocol.RECOVERY:
				connection.getLogger().debug("Reception: Switch to Recovery");
				switchProcessor(new ConnectionProcessorRecovery());
				break;
			case Protocol.BACKDOOR:
				connection.getLogger().debug("Reception: Switch to Backdoor");
				switchProcessor(new ConnectionProcessorBackdoorAuthorization());
				break;
			default:
				throw new ProtocolException(String.format("Reception: Invalid flag 0x%s (%s)", Integer.toString(flag & 0xFF, 16), (char)(flag & 0xFF)));
		}
	}
}
