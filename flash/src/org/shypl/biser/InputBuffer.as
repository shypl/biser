package org.shypl.biser
{
	import flash.utils.ByteArray;

	public class InputBuffer
	{
		private const _bytes:ByteArray = new ByteArray();

		public function InputBuffer(bytes:ByteArray)
		{
			_bytes.writeBytes(bytes, 0, bytes.length);
			_bytes.position = 0;
		}

		public function readBool():Boolean
		{
			return _bytes.readBoolean();
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

		public function readByte():int
		{
			return _bytes.readByte();
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

		public function readDouble():Number
		{
			return _bytes.readDouble();
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
					return (_bytes.readUnsignedByte() << 16) + (_bytes.readUnsignedByte() << 8)
						+ _bytes.readUnsignedByte();

				case 0xFB:
					return (_bytes.readUnsignedByte() << 8) + _bytes.readUnsignedByte();

				case 0xFA:
					return _bytes.readUnsignedByte();

				default:
					return b;
			}
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

		public function readString():String
		{
			const l:int = readInt();
			return (l === -1) ? null : _bytes.readUTFBytes(l);
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

		public function readUint():uint
		{
			var b:uint = _bytes.readUnsignedByte();

			switch (b) {
				case 0xFF:
					return _bytes.readUnsignedInt();

				case 0xFE:
					return (_bytes.readUnsignedByte() << 16) + (_bytes.readUnsignedByte() << 8)
						+ _bytes.readUnsignedByte();

				case 0xFD:
					return (_bytes.readUnsignedByte() << 8) + _bytes.readUnsignedByte();

				case 0xFC:
					return _bytes.readUnsignedByte();

				default:
					return b;
			}
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
	}
}
