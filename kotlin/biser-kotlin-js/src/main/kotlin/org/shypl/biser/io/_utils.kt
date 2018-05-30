package org.shypl.biser.io

fun Byte.toUint(): Int {
	return toInt() and 0xFF
}