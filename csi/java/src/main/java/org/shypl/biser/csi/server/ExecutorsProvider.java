package org.shypl.biser.csi.server;

import java.util.concurrent.ScheduledExecutorService;

public interface ExecutorsProvider {
	ScheduledExecutorService getServerExecutorService();
	
	ScheduledExecutorService getConnectionExecutorService();
	
	ScheduledExecutorService getClientExecutorService();
}
