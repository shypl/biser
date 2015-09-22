package org.shypl.biser.io;

import java.io.IOException;
import java.util.Map;

class MapEncoder<K, V> implements Encoder<Map<K, V>> {
	private final Encoder<K> keyEncoder;
	private final Encoder<V> valueEncoder;

	public MapEncoder(Encoder<K> keyEncoder, Encoder<V> valueEncoder) {
		this.keyEncoder = keyEncoder;
		this.valueEncoder = valueEncoder;
	}

	@Override
	public void encode(Map<K, V> value, BiserWriter writer) throws IOException {
		writer.writeMap(value, keyEncoder, valueEncoder);
	}

	private static class CacheKey {
		public Encoder<?> keyEncoder;
		public Encoder<?> valueEncoder;

		public CacheKey(Encoder<?> keyEncoder, Encoder<?> valueEncoder) {
			this.keyEncoder = keyEncoder;
			this.valueEncoder = valueEncoder;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}
}
