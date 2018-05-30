package org.shypl.biser.io

import org.shypl.biser.StringUtils

abstract class Entity {
	override fun toString(): String {
		val fields = LinkedHashMap<String, String>()
		_toString(fields)
		return StringUtils.toString(fields)
	}
	
	open fun _id(): Int {
		return 0
	}
	
	open fun _encode(writer: DataWriter) {}
	
	open fun _decode(reader: DataReader) {}
	
	protected open fun _toString(fields: MutableMap<String, String>) {}
}