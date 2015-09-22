package org.shypl.biser.api.server;

import org.shypl.biser.api.ProtocolException;

abstract class ConnectionStrategy {
	protected Connection connection;

	public abstract void handleData() throws ProtocolException;

	public abstract void handleClose(boolean broken);

	void setConnection(Connection connection) {
		this.connection = connection;
	}
}
