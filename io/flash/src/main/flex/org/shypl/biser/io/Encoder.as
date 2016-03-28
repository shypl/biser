package org.shypl.biser.io {
	public interface Encoder {
		function encode(value:Object, writer:DataWriter):void;
	}
}
