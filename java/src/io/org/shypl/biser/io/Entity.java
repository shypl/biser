package org.shypl.biser.io;

import java.io.IOException;

public abstract class Entity {
	protected int _id() {
		return 0;
	}

	protected void _encode(BiserWriter writer) throws IOException {}

	protected void _decode(BiserReader reader) throws IOException {}
}
