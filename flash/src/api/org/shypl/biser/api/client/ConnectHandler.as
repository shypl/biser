package org.shypl.biser.api.client {
	public interface ConnectHandler {
		function handlerConnectSuccess():void;

		function handlerConnectFail(reason:ConnectionCloseReason):void;
	}
}
