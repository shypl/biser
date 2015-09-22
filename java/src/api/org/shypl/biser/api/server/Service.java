package org.shypl.biser.api.server;

import org.shypl.biser.io.ByteArrayReader;
import org.shypl.biser.io.ByteArrayWriter;

public abstract class Service<C extends AbstractClient> {
	private ApiGate<C> gate;

	void setGate(ApiGate<C> gate) {
		this.gate = gate;
	}

	protected abstract void _executeAction(int id, ByteArrayReader reader, ByteArrayWriter writer) throws Exception;

	protected final C getClient() {
		return gate.getCurrentClient();
	}

	protected final void _log(String message, Object... args) {
		getClient().getLogger().trace(message, args);
	}
}
