package org.shypl.biser.io;

import java.util.Date;
import java.util.Map;

public interface Decoder<T> {
	Decoder<Byte>    BYTE   = new TypedDecoder<Byte>(Byte.class) {
		@Override
		public Byte decode(DataReader reader) {
			return reader.readByte();
		}
	};
	Decoder<Boolean> BOOL   = new TypedDecoder<Boolean>(Boolean.class) {
		@Override
		public Boolean decode(DataReader reader) {
			return reader.readBool();
		}
	};
	Decoder<Integer> INT    = new TypedDecoder<Integer>(Integer.class) {
		@Override
		public Integer decode(DataReader reader) {
			return reader.readInt();
		}
	};
	Decoder<Integer> UINT   = new TypedDecoder<Integer>(Integer.class) {
		@Override
		public Integer decode(DataReader reader) {
			return reader.readUint();
		}
	};
	Decoder<Long>    LONG   = new TypedDecoder<Long>(Long.class) {
		@Override
		public Long decode(DataReader reader) {
			return reader.readLong();
		}
	};
	Decoder<Long>    ULONG  = new TypedDecoder<Long>(Long.class) {
		@Override
		public Long decode(DataReader reader) {
			return reader.readUlong();
		}
	};
	Decoder<Double>  DOUBLE = new TypedDecoder<Double>(Double.class) {
		@Override
		public Double decode(DataReader reader) {
			return reader.readDouble();
		}
	};
	Decoder<byte[]>  BYTES  = new TypedDecoder<byte[]>(byte[].class) {
		@Override
		public byte[] decode(DataReader reader) {
			return reader.readBytes();
		}
	};
	Decoder<String>  STRING = new TypedDecoder<String>(String.class) {
		@Override
		public String decode(DataReader reader) {
			return reader.readString();
		}
	};
	Decoder<Date>    DATE   = new TypedDecoder<Date>(Date.class) {
		@Override
		public Date decode(DataReader reader) {
			return reader.readDate();
		}
	};

	Decoder<byte[]>    BYTE_ARRAY   = new TypedDecoder<byte[]>(byte[].class) {
		@Override
		public byte[] decode(DataReader reader) {
			return reader.readByteArray();
		}
	};
	Decoder<boolean[]> BOOL_ARRAY   = new TypedDecoder<boolean[]>(boolean[].class) {
		@Override
		public boolean[] decode(DataReader reader) {
			return reader.readBoolArray();
		}
	};
	Decoder<int[]>     INT_ARRAY    = new TypedDecoder<int[]>(int[].class) {
		@Override
		public int[] decode(DataReader reader) {
			return reader.readIntArray();
		}
	};
	Decoder<int[]>     UINT_ARRAY   = new TypedDecoder<int[]>(int[].class) {
		@Override
		public int[] decode(DataReader reader) {
			return reader.readUintArray();
		}
	};
	Decoder<long[]>    LONG_ARRAY   = new TypedDecoder<long[]>(long[].class) {
		@Override
		public long[] decode(DataReader reader) {
			return reader.readLongArray();
		}
	};
	Decoder<long[]>    ULONG_ARRAY  = new TypedDecoder<long[]>(long[].class) {
		@Override
		public long[] decode(DataReader reader) {
			return reader.readUlongArray();
		}
	};
	Decoder<double[]>  DOUBLE_ARRAY = new TypedDecoder<double[]>(double[].class) {
		@Override
		public double[] decode(DataReader reader) {
			return reader.readDoubleArray();
		}
	};

	static <E> Decoder<E[]> forArray(Decoder<E> elementDecoder) {
		return ArrayDecoder.factory(elementDecoder);
	}

	static <K, V> Decoder<Map<K, V>> forMap(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
		return new MapDecoder<>(keyDecoder, valueDecoder);
	}

	T decode(DataReader reader);

	T[] createArray(int length);
}
