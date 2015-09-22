package org.shypl.biser.io;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public interface Decoder<T> {
	Decoder<Byte>    BYTE   = new TypedDecoder<Byte>(Byte.class) {
		@Override
		public Byte decode(BiserReader reader) throws IOException {
			return reader.readByte();
		}
	};
	Decoder<Boolean> BOOL   = new TypedDecoder<Boolean>(Boolean.class) {
		@Override
		public Boolean decode(BiserReader reader) throws IOException {
			return reader.readBool();
		}
	};
	Decoder<Integer> INT    = new TypedDecoder<Integer>(Integer.class) {
		@Override
		public Integer decode(BiserReader reader) throws IOException {
			return reader.readInt();
		}
	};
	Decoder<Integer> UINT   = new TypedDecoder<Integer>(Integer.class) {
		@Override
		public Integer decode(BiserReader reader) throws IOException {
			return reader.readUint();
		}
	};
	Decoder<Long>    LONG   = new TypedDecoder<Long>(Long.class) {
		@Override
		public Long decode(BiserReader reader) throws IOException {
			return reader.readLong();
		}
	};
	Decoder<Long>    ULONG  = new TypedDecoder<Long>(Long.class) {
		@Override
		public Long decode(BiserReader reader) throws IOException {
			return reader.readUlong();
		}
	};
	Decoder<Double>  DOUBLE = new TypedDecoder<Double>(Double.class) {
		@Override
		public Double decode(BiserReader reader) throws IOException {
			return reader.readDouble();
		}
	};
	Decoder<byte[]>  BYTES  = new TypedDecoder<byte[]>(byte[].class) {
		@Override
		public byte[] decode(BiserReader reader) throws IOException {
			return reader.readBytes();
		}
	};
	Decoder<String>  STRING = new TypedDecoder<String>(String.class) {
		@Override
		public String decode(BiserReader reader) throws IOException {
			return reader.readString();
		}
	};
	Decoder<Date>    DATE   = new TypedDecoder<Date>(Date.class) {
		@Override
		public Date decode(BiserReader reader) throws IOException {
			return reader.readDate();
		}
	};

	Decoder<byte[]>    BYTE_ARRAY   = new TypedDecoder<byte[]>(byte[].class) {
		@Override
		public byte[] decode(BiserReader reader) throws IOException {
			return reader.readByteArray();
		}
	};
	Decoder<boolean[]> BOOL_ARRAY   = new TypedDecoder<boolean[]>(boolean[].class) {
		@Override
		public boolean[] decode(BiserReader reader) throws IOException {
			return reader.readBoolArray();
		}
	};
	Decoder<int[]>     INT_ARRAY    = new TypedDecoder<int[]>(int[].class) {
		@Override
		public int[] decode(BiserReader reader) throws IOException {
			return reader.readIntArray();
		}
	};
	Decoder<int[]>     UINT_ARRAY   = new TypedDecoder<int[]>(int[].class) {
		@Override
		public int[] decode(BiserReader reader) throws IOException {
			return reader.readUintArray();
		}
	};
	Decoder<long[]>    LONG_ARRAY   = new TypedDecoder<long[]>(long[].class) {
		@Override
		public long[] decode(BiserReader reader) throws IOException {
			return reader.readLongArray();
		}
	};
	Decoder<long[]>    ULONG_ARRAY  = new TypedDecoder<long[]>(long[].class) {
		@Override
		public long[] decode(BiserReader reader) throws IOException {
			return reader.readUlongArray();
		}
	};
	Decoder<double[]>  DOUBLE_ARRAY = new TypedDecoder<double[]>(double[].class) {
		@Override
		public double[] decode(BiserReader reader) throws IOException {
			return reader.readDoubleArray();
		}
	};

	static <E> Decoder<E[]> forArray(Decoder<E> elementDecoder) {
		return ArrayDecoder.factory(elementDecoder);
	}

	static <K, V> Decoder<Map<K, V>> forMap(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
		return new MapDecoder<>(keyDecoder, valueDecoder);
	}

	T decode(BiserReader reader) throws IOException;

	T[] createArray(int length);
}
