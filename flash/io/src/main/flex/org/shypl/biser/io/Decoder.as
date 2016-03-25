package org.shypl.biser.io {
	public interface Decoder {
		function createVector(size:int):Object;

		function decode(reader:DataReader):Object;
	}
}
