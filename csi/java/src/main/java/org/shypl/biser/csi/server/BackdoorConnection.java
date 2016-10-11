package org.shypl.biser.csi.server;

public interface BackdoorConnection {
	void writeInt(int i);
	
	void writeLong(long l);
	
	void writeString(String s);
	
	void writeLine(String s);
	
	void close();
}
