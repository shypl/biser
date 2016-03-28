package org.shypl.biser.io {
	import flash.utils.ByteArray;

	public class Utils {
		public static function extractBytes(byteArray:ByteArray):Array {
			if (byteArray == null) {
				return null;
			}

			byteArray.position = 0;
			const bytes:Array = [];
			while (byteArray.bytesAvailable > 0) {
				bytes.push(byteArray.readByte());
			}
			return bytes;
		}

		public static function wrapBytes(bytes:Array):ByteArray {
			const a:ByteArray = new ByteArray();
			for each (var b:int in bytes) {
				a.writeByte(b);
			}
			a.position = 0;
			return a;
		}

		public static function intsToBytes(ints:Array):Array {
			for (var i:int = 0; i < ints.length; i++) {
				var v:int = ints[i];
				if (v > 127) {
					ints[i] = v - 256;
				}
			}
			return ints;
		}
	}
}
