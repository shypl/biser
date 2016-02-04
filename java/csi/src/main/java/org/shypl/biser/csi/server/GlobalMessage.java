package org.shypl.biser.csi.server;

import org.shypl.biser.io.ByteArrayWriter;
import org.slf4j.Logger;

public abstract class GlobalMessage {
	private final int serviceId;
	private final int actionId;

	public GlobalMessage(int serviceId, int actionId) {
		this.serviceId = serviceId;
		this.actionId = actionId;
	}

	protected abstract void make(ByteArrayWriter writer, Logger logger);

	void send(CsiServer server) {
		ByteArrayWriter writer = new ByteArrayWriter();
		writer.writeInt(0);
		writer.writeInt(serviceId);
		writer.writeInt(actionId);

		make(writer, server.getLogger());

		server.sendMessageForAllClients(writer.toByteArray());
	}
}
