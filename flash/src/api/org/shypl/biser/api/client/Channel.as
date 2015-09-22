package org.shypl.biser.api.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataOutput;

	public interface Channel {
		function writeByte(byte:int):void;

		function writeBytes(bytes:ByteArray):void;

		function close():void;
	}
}
