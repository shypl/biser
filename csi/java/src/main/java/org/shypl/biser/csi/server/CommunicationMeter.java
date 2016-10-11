package org.shypl.biser.csi.server;

public interface CommunicationMeter {
	ServerCallMeter registerServerCall(String service, String method);
	
	void registerClientCall(String service, String method);
}
