package org.shypl.biser.csi {
	import org.shypl.asak.lang.Exception;

	public class CsiException extends Exception {
		public function CsiException(message:String = null, cause:Error = null) {
			super(message, cause);
		}
	}
}
