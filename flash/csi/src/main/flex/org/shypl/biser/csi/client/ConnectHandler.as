package org.shypl.biser.csi.client {
	public interface ConnectHandler {
		function handleConnectSuccess():void;

		function handleConnectFail(reason:ConnectionCloseReason):void;
	}
}
