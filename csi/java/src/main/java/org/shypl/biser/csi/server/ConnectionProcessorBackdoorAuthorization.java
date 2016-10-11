package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ConnectionCloseReason;
import org.shypl.biser.csi.ProtocolException;

public class ConnectionProcessorBackdoorAuthorization extends ConnectionProcessor {
	
	private ConnectionStringLineBuffer receivedPassword = new ConnectionStringLineBuffer();
	
	@Override
	public void processData() throws ProtocolException {
		if (receivedPassword.fill(connection)) {
			if (receivedPassword.read().equals(getPassword())) {
				switchProcessor(new ConnectionProcessorBackdoorMessaging());
			}
			else {
				connection.close(ConnectionCloseReason.NONE);
			}
		}
	}
	
	private String getPassword() {
		return connection.getServer().getSettings().getBackdoorPassword();
	}
}
