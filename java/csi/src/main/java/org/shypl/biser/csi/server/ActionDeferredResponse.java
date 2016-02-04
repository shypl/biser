package org.shypl.biser.csi.server;

import org.shypl.biser.io.ByteArrayWriter;

public abstract class ActionDeferredResponse {
	protected ByteArrayWriter writer;
	protected AbstractClient  client;

	public ActionDeferredResponse(AbstractClient client, int responseId) {
		this.client = client;
		writer = new ByteArrayWriter();
		writer.writeInt(responseId);
	}

	protected final void _send() {
		client.sendMessage(writer.toByteArray());
		writer = null;
		client = null;
	}

	protected final void _log(String message, Object... args) {
		client.getLogger().trace(message, args);
	}
}
