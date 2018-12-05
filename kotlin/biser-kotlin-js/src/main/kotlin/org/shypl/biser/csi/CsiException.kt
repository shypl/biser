package org.shypl.biser.csi

class CsiException(message: String, cause: Throwable?) : RuntimeException(message, cause) {
	constructor(message: String) : this(message, null)
	
}