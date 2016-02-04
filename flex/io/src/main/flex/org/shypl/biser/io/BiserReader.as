package org.shypl.biser.io {
	import flash.utils.ByteArray;

	import org.shypl.common.collection.Map;
	import org.shypl.common.lang.Enum;
	import org.shypl.common.math.Long;

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
