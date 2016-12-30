package org.shypl.biser.io;

import org.shypl.common.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Entity {
	@Override
	public String toString() {
		Map<String, String> fields = new LinkedHashMap<>();
		_toString(fields);
		return StringUtils.toString(fields);
	}
	
	public int _id() {
		return 0;
	}
	
	protected void _encode(DataWriter writer) {
	}
	
	protected void _decode(DataReader reader) {
	}
	
	protected void _toString(Map<String, String> fields) {
	}
}
