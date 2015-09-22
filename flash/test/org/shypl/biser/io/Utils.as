package org.shypl.biser.io {
	import flash.utils.ByteArray;

	public class Utils {
		private static const hexArray:Array = "0123456789ABCDEF".split("");

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

		public static function getHexString(bytes:Array):String {
			var hexChars:String = "";
			for (var j:int = 0; j < bytes.length; j++) {
				var v:int = bytes[j] & 0xFF;
				hexChars += '0';
				hexChars += 'x';
				hexChars += hexArray[v >>> 4];
				hexChars += hexArray[v & 0x0F];
				hexChars += ',';
				hexChars += ' ';
			}
			return hexChars;
		}
	}
}
