package org.shypl.biser.csi.server;

import org.shypl.biser.csi.CommunicationLoggingUtils;
import org.shypl.biser.io.ByteArrayOutputData;
import org.shypl.biser.io.DataWriter;

public abstract class DeferredResponse {
	protected DataWriter          _writer;
	private   String              serviceName;
	private   String              methodName;
	private   Client              client;
	private   ByteArrayOutputData data;

	protected DeferredResponse(Client client, int responseId, String serviceName, String methodName) {
		this.client = client;
		this.serviceName = serviceName;
		this.methodName = methodName;

		data = new ByteArrayOutputData();
		_writer = data.getWriter();
		_writer.writeInt(responseId);
	}

	protected final void _send() {
		client.sendMessage(data.getArray());
		client = null;
		data = null;
		_writer = null;
	}

	protected final void _log(Object result) {
		CommunicationLoggingUtils.logServerResponse(client.getLogger(), serviceName, methodName, result);
	}
}
