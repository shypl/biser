package org.shypl.biser.io {
	import flash.utils.ByteArray;

	import org.shypl.asak.collection.Map;
	import org.shypl.asak.lang.Enum;
	import org.shypl.asak.math.Long;

	public interface BiserReader {
		function readByte():int;

		function readBool():Boolean;

		function readInt():int;

		function readUint():uint;

		function readLong():Long;

		function readUlong():Long;

		function readDouble():Number;

		function readBytes():ByteArray;

		function readString():String;

		function readDate():Date;

		function readEnum(type:Class):Enum;

		function readEntity(type:Class):Entity;

		function readArray(elementDecoder:Decoder):Object;

		function readMap(keyDecoder:Decoder, valueDecoder:Decoder):Map;
	}
}
