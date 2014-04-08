package org.shypl.biser
{
	import flash.utils.ByteArray;

	import org.shypl.common.lang.OrdinalEnum;

	public class OutputBuffer
	{
		private const _bytes:ByteArray = new ByteArray();

		public function get size():int
		{
			return _bytes.position;
		}

		public function clear():void
		{
			clearTo(0);
		}

		public function clearTo(length:uint):void
		{
			_bytes.position = length;
			_bytes.length = length;
		}

		public function getBytes():ByteArray
		{
			const bytes:ByteArray = new ByteArray();
			bytes.writeBytes(_bytes, 0, _bytes.length);
			bytes.position = 0;
			return bytes;
		}

		public function writeBool(v:Boolean):void
		{
			_bytes.writeByte(v ? 0x01 : 0x00);
		}

		public function writeBoolArray(v:Vector.<Boolean>):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:Boolean in v) {
					_bytes.writeByte(e ? 1 : 0);
				}
			}
		}

		public function writeByte(v:int):void
		{
			if (v < -128 || v > 127) {
				throw new ArgumentError();
			}
			_bytes.writeByte(v);
		}

		public function writeByteArray(v:Vector.<int>):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:int in v) {
					writeByte(e);
				}
			}
		}

		public function writeBytes(v:ByteArray):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				_bytes.writeBytes(v);
			}
		}

		public function writeDouble(v:Number):void
		{
			_bytes.writeDouble(v);
		}

		public function writeDoubleArray(v:Vector.<Number>):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:Number in v) {
					writeDouble(e);
				}
			}
		}

		public function writeEntity(v:BiserEntity):void
		{
			if (v === null) {
				writeInt(-1);
			}
			else {
				writeInt(v.__eid);
				v.encode(this);
			}
		}

		public function writeEntityArray(v:Object):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:BiserEntity in v) {
					writeEntity(e);
				}
			}
		}

		public function writeEnum(v:OrdinalEnum):void
		{
			writeInt(v === null ? -1 : v.ordinal);
		}

		public function writeEnumArray(v:Object):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:OrdinalEnum in v) {
					writeEnum(e);
				}
			}
		}

		public function writeInt(v:int):void
		{
			if (v >= 0) {

				if (v <= 0xF9) {
					_bytes.writeByte(v);
					return;
				}

				if (v <= 0xFF) {
					_bytes.writeByte(0xFA);
					_bytes.writeByte(v);
					return;
				}

				if (v <= 0xFFFF) {
					_bytes.writeByte(0xFB);
					_bytes.writeByte(v >>> 8);
					_bytes.writeByte(v);
					return;
				}

				if (v <= 0xFFFFFF) {
					_bytes.writeByte(0xFC);
					_bytes.writeByte(v >>> 16);
					_bytes.writeByte(v >>> 8);
					_bytes.writeByte(v);
					return;
				}

			}
			else if (v == -1) {
				_bytes.writeByte(0xFD);
				return;
			}
			else if (v >= -257) {
				_bytes.writeByte(0xFE);
				_bytes.writeByte(-v - 2);
				return;
			}

			_bytes.writeByte(0xFF);
			_bytes.writeInt(v);
		}

		public function writeIntArray(v:Vector.<int>):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:int in v) {
					writeInt(e);
				}
			}
		}

		public function writeString(v:String):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else if (v.length == 0) {
				writeInt(0);
			}
			else {
				const bytes:ByteArray = new ByteArray();
				bytes.writeUTFBytes(v);
				writeInt(bytes.length);
				_bytes.writeBytes(bytes);
			}
		}

		public function writeStringArray(v:Vector.<String>):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:String in v) {
					writeString(e);
				}
			}
		}

		public function writeUint(v:uint):void
		{
			if (v >= 0) {

				if (v <= 0xFB) {
					_bytes.writeByte(v);
					return;
				}

				if (v <= 0xFF) {
					_bytes.writeByte(0xFC);
					_bytes.writeByte(v);
					return;
				}

				if (v <= 0xFFFF) {
					_bytes.writeByte(0xFD);
					_bytes.writeByte(v >>> 8);
					_bytes.writeByte(v);
					return;
				}

				if (v <= 0xFFFFFF) {
					_bytes.writeByte(0xFE);
					_bytes.writeByte(v >>> 16);
					_bytes.writeByte(v >>> 8);
					_bytes.writeByte(v);
					return;
				}

			}

			_bytes.writeByte(0xFF);
			_bytes.writeUnsignedInt(v);
		}

		public function writeUintArray(v:Vector.<uint>):void
		{
			if (v == null) {
				writeInt(-1);
			}
			else {
				writeInt(v.length);
				for each (var e:uint in v) {
					writeUint(e);
				}
			}
		}
	}
}
