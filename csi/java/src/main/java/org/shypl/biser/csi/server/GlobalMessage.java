package org.shypl.biser.csi.server;

import org.shypl.biser.csi.CommunicationLoggingUtils;
import org.shypl.biser.io.ByteArrayOutputData;
import org.shypl.biser.io.DataWriter;
import org.slf4j.Logger;

import java.util.Collection;

public abstract class GlobalMessage {
	private static final ThreadLocal<ByteArrayOutputData> threadLocalData = new ThreadLocal<ByteArrayOutputData>() {
		@Override
		protected ByteArrayOutputData initialValue() {
			return new ByteArrayOutputData();
		}
	};

	private final int    serviceId;
	private final String serviceName;
	private final int    methodId;
	private final String methodName;
	private       Logger logger;

	protected GlobalMessage(int serviceId, String serviceName, int methodId, String methodName) {
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.methodId = methodId;
		this.methodName = methodName;
	}

	protected abstract void make(DataWriter writer);

	protected final void log() {
		CommunicationLoggingUtils.logGlobalServerCall(logger, serviceName, methodName);
	}

	protected final void log(Object arg) {
		CommunicationLoggingUtils.logGlobalServerCall(logger, serviceName, methodName, arg);
	}

	protected final void log(Object arg1, Object arg2) {
		CommunicationLoggingUtils.logGlobalServerCall(logger, serviceName, methodName, arg1, arg2);
	}

	protected final void log(Object... args) {
		CommunicationLoggingUtils.logGlobalServerCall(logger, serviceName, methodName, args);
	}

	void send(Collection<? extends AbstractClient> clients, Logger logger) {
		this.logger = logger;

		ByteArrayOutputData data = threadLocalData.get();
		data.clear();
		DataWriter writer = data.getWriter();

		writer.writeInt(0);
		writer.writeInt(serviceId);
		writer.writeInt(methodId);
		make(writer);

		byte[] bytes = data.getArray();

		for (AbstractClient client : clients) {
			client.sendMessage(bytes);
		}
	}
}
