package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;

	public interface Channel {
		function writeByte(byte:int):void;

		function writeBytes(bytes:ByteArray):void;
		
		function close():void;
	}
}
