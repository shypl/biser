package org.shypl.biser.csi;

public class CsiException extends Exception {
	public CsiException(String message) {
		super(message);
	}

	public CsiException(String message, Throwable cause) {
		super(message, cause);
	}
}
