package org.shypl.biser.api;

import org.shypl.biser.OutputBuffer;

public interface ConnectionChannel
{
	void sendMessage(final OutputBuffer message);

	void close();
}
