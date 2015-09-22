package org.shypl.biser.api.server;

import org.shypl.biser.io.ByteArrayWriter;
import org.slf4j.Logger;

public abstract class GlobalMessage {
	private final int serviceId;
	private final int actionId;

	public GlobalMessage(int serviceId, int actionId) {
		this.serviceId = serviceId;
		this.actionId = actionId;
	}

	void send(ApiServer server) {
		ByteArrayWriter writer = new ByteArrayWriter();
		writer.writeInt(0);
		writer.writeInt(serviceId);
		writer.writeInt(actionId);

		make(writer, server.getLogger());

		server.sendMessageForAllClients(writer.toByteArray());
	}

	protected abstract void make(ByteArrayWriter writer, Logger logger);
}
