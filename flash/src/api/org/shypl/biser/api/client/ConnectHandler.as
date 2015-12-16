package org.shypl.biser.api.client {
	public interface ConnectHandler {
		function handleConnectSuccess():void;

		function handleConnectFail(reason:ConnectionCloseReason):void;
	}
}
