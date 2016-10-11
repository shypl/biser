package org.shypl.biser.csi.server;

import java.util.List;

public interface BackdoorCommandHandler {
	void handleBackdoorCommand(BackdoorConnection connection, String command, String[] arguments);
}
