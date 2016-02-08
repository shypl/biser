package org.shypl.biser.io {
	import flash.utils.ByteArray;
	import flash.utils.IDataOutput;

	import org.shypl.common.collection.Map;
	import org.shypl.common.lang.Enum;
	import org.shypl.common.math.Long;

	public interface BiserWriter {

		function BiserWriter(stream:IDataOutput);

		function writeByte(value:int):void;

		function writeBool(value:Boolean):void;

		function writeInt(value:int):void;

		function writeUint(value:uint):void;

		function writeLong(value:Long):void;

		function writeUlong(value:Long):void;

		function writeDouble(value:Number):void;

		function writeBytes(value:ByteArray):void;

		function writeString(value:String):void;

		function writeDate(value:Date):void;

		function writeEnum(value:Enum):void;

		function writeEntity(value:Entity):void;

		function writeArray(array:Object, elementEncoder:Encoder):void;

		function writeMap(map:Map, keyEncoder:Encoder, valueEncoder:Encoder):void;
	}
}
