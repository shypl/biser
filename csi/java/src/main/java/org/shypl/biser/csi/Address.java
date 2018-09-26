package org.shypl.biser.csi;

@SuppressWarnings("WeakerAccess")
public class Address {
	
	public final String host;
	public final int    fsPort;
	public final int    wsPort;
	public final String wssCrtFile;
	public final String wssKeyFile;
	
	public Address(String host, int fsPort, int wsPort, String wssCrtFile, String wssKeyFile) {
		this.host = host;
		this.fsPort = fsPort;
		this.wsPort = wsPort;
		this.wssCrtFile = wssCrtFile;
		this.wssKeyFile = wssKeyFile;
	}
	
	public boolean isWss() {
		return wsPort != 0 && wssCrtFile != null;
	}
	
	public boolean isWs() {
		return wsPort != 0 && wssCrtFile == null;
	}
	
	public boolean isFs() {
		return fsPort != 0;
	}
}
