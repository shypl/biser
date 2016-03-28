package org.shypl.biser.io;

import java.util.Map;

class MapDecoder<K, V> implements Decoder<Map<K, V>> {
	private final Decoder<K> keyDecoder;
	private final Decoder<V> valueDecoder;

	public MapDecoder(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
		this.keyDecoder = keyDecoder;
		this.valueDecoder = valueDecoder;
	}

	@Override
	public Map<K, V> decode(DataReader reader) {
		return reader.readMap(keyDecoder, valueDecoder);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<K, V>[] createArray(int length) {
		return new Map[length];
	}
}
