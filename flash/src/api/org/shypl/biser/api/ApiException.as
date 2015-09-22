package org.shypl.biser.api {
	import org.shypl.common.lang.Exception;

	public class ApiException extends Exception {
		public function ApiException(message:String = null, cause:Error = null) {
			super(message, cause);
		}
	}
}
