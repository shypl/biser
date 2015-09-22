package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CodeNamedObjectSet<E extends CodeNamedObject> implements Iterable<E> {

	private final Map<String, E> map = new LinkedHashMap<>();

	public E add(E e) {
		if (map.containsKey(e.getName())) {
			throw new IllegalArgumentException();
		}
		map.put(e.getName(), e);

		return e;
	}

	public E get(String name) {
		return map.get(name);
	}

	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	}

	public List<E> getElements() {
		return new ArrayList<>(map.values());
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}
}
