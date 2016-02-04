package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ProtocolException;

abstract class ConnectionStrategy {
	protected Connection connection;

	public abstract void handleData() throws ProtocolException;

	public abstract void handleClose(boolean broken);

	void setConnection(Connection connection) {
		this.connection = connection;
	}
}
