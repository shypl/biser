package org.shypl.biser
{
	import flash.utils.ByteArray;

	public class InputBuffer
	{
		private var _bytes:ByteArray;

		public function InputBuffer(bytes:ByteArray)
		{
			this._bytes = bytes;
		}

		public function readBool():Boolean
		{
			return _bytes.readBoolean();
		}

		public function readByte():int
		{
			return _bytes.readByte();
		}

		public function readShort():int
		{
			return _bytes.readShort();
		}

		public function readInt():int
		{
			var b:uint = _bytes.readUnsignedByte();

			switch (b) {
				case 0xFF:
					return _bytes.readInt();

				case 0xFE:
					return -(_bytes.readUnsignedByte() + 2);

				case 0xFD:
					return -1;

				case 0xFC:
					return (_bytes.readUnsignedByte() << 16) + (_bytes.readUnsignedByte() << 8) + _bytes.readUnsignedByte();

				case 0xFB:
					return (_bytes.readUnsignedByte() << 8) + _bytes.readUnsignedByte();

				case 0xFA:
					return _bytes.readUnsignedByte();

				default:
					return b;
			}
		}

		public function readUint():uint
		{
			var b:uint = _bytes.readUnsignedByte();

			switch (b) {
				case 0xFF:
					return _bytes.readUnsignedInt();

				case 0xFE:
					return (_bytes.readUnsignedByte() << 16) + (_bytes.readUnsignedByte() << 8) + _bytes.readUnsignedByte();

				case 0xFD:
					return (_bytes.readUnsignedByte() << 8) + _bytes.readUnsignedByte();

				case 0xFC:
					return _bytes.readUnsignedByte();

				default:
					return b;
			}
		}

		public function readDouble():Number
		{
			return _bytes.readDouble();
		}

		public function readString():String
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}
			return _bytes.readUTFBytes(l);
		}

		public function readBoolArray():Vector.<Boolean>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<Boolean> = new Vector.<Boolean>(l, true);
			for (var i:int = 0; i < l; ++i) {
				a[i] = readBool();
			}

			return a;
		}

		public function readByteArray():Vector.<int>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<int> = new Vector.<int>(l, true);

			for (var i:uint = 0; i < l; ++i) {
				a[i] = readByte();
			}

			return a;
		}

		public function readShortArray():Vector.<int>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<int> = new Vector.<int>(l, true);

			for (var i:int = 0; i < l; ++i) {
				a[i] = readShort();
			}

			return a;
		}

		public function readIntArray():Vector.<int>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<int> = new Vector.<int>(l, true);

			for (var i:int = 0; i < l; ++i) {
				a[i] = readInt();
			}

			return a;
		}

		public function readUintArray():Vector.<uint>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<uint> = new Vector.<uint>(l, true);

			for (var i:int = 0; i < l; ++i) {
				a[i] = readUint();
			}

			return a;
		}

		public function readDoubleArray():Vector.<Number>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<Number> = new Vector.<Number>(l, true);

			for (var i:int = 0; i < a.length; ++i) {
				a[i] = readDouble();
			}

			return a;
		}

		public function readStringArray():Vector.<String>
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:Vector.<String> = new Vector.<String>(l, true);

			for (var i:int = 0; i < l; ++i) {
				a[i] = readString();
			}

			return a;
		}

		public function readBytes():ByteArray
		{
			const l:int = readInt();
			if (l === -1) {
				return null;
			}

			const a:ByteArray = new ByteArray();
			a.writeBytes(_bytes, _bytes.position, l);
			_bytes.position += l;
			a.position = 0;
			return a;
		}
	}
}
