package org.shypl.biser.server;

public interface ClientChannel
{
	void write(byte[] data);

	void close();
}
