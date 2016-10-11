package org.shypl.biser.csi.server;

class CommunicationMeterFake implements CommunicationMeter {
	public static final CommunicationMeter INSTANCE = new CommunicationMeterFake();
	
	private final ServerCallMeter serverCallMeter = new ServerCallMeterFake();
	
	private CommunicationMeterFake() {
	}
	
	@Override
	public ServerCallMeter registerServerCall(String service, String method) {
		return serverCallMeter;
	}
	
	@Override
	public void registerClientCall(String service, String method) {
	}
	
	private static class ServerCallMeterFake implements ServerCallMeter {
		@Override
		public void complete() {
		}
	}
}
