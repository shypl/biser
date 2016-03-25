package org.shypl.biser.io;

import java.util.Map;

class MapEncoder<K, V> implements Encoder<Map<K, V>> {
	private final Encoder<K> keyEncoder;
	private final Encoder<V> valueEncoder;

	public MapEncoder(Encoder<K> keyEncoder, Encoder<V> valueEncoder) {
		this.keyEncoder = keyEncoder;
		this.valueEncoder = valueEncoder;
	}

	@Override
	public void encode(Map<K, V> value, DataWriter writer) {
		writer.writeMap(value, keyEncoder, valueEncoder);
	}
}
