package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ProtocolException;

abstract class ConnectionProcessor {
	protected Connection connection;

	abstract public void processData() throws ProtocolException;

	public void processClose(boolean breaking) {
	}

	public void init(Connection connection) {
		this.connection = connection;
	}

	public void destroy() {
		connection = null;
	}

	protected void switchProcessor(ConnectionProcessor processor) {
		connection.setProcessor(processor);
		destroy();
	}
}
