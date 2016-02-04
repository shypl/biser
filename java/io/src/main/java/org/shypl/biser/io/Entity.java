package org.shypl.biser.io;

import org.shypl.common.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Entity {
	@Override
	public String toString() {
		Map<String, String> fields = new LinkedHashMap<>();
		_toString(fields);
		return StringUtils.toString(fields);
	}

	protected int _id() {
		return 0;
	}

	protected void _encode(BiserWriter writer) throws IOException {
	}

	protected void _decode(BiserReader reader) throws IOException {
	}

	protected void _toString(Map<String, String> fields) {
	}
}
