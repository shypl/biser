package org.shypl.biser

import org.shypl.biser.io.toUint

object StringUtils {
	private const val NULL = "<null>"
	private const val TRUE = "<true>"
	private const val FALSE = "<false>"
	private const val SEQUENCE_SEPARATOR = ", "
	private const val KEY_VALUE_SEPARATOR = ": "
	private const val LIST_EMPTY = "[]"
	private const val MAP_EMPTY = "{}"
	private const val LIST_OPEN = "["
	private const val LIST_CLOSE = "]"
	private const val MAP_OPEN = "{"
	private const val MAP_CLOSE = "}"
	
	fun toString(v: Any?): String {
		return when (v) {
			null             -> NULL
			is Boolean       -> toString(v)
			is Byte          -> toString(v)
			is Int           -> toString(v)
			is Long          -> toString(v)
			is Array<*>      -> toString(v)
			is Collection<*> -> toString(v)
			is Map<*, *>     -> toString(v)
			is BooleanArray  -> toString(v)
			is ByteArray     -> toString(v)
			is CharArray     -> toString(v)
			is ShortArray    -> toString(v)
			is IntArray      -> toString(v)
			is LongArray     -> toString(v)
			is FloatArray    -> toString(v)
			is DoubleArray   -> toString(v)
			else             -> v.toString()
		}
		
	}
	
	fun toString(v: Boolean): String {
		return if (v) TRUE else FALSE
	}
	
	fun toString(v: Byte): String {
		val s = v.toUint().toString(16)
		return if (s.length == 1) "0$s" else s
	}
	
	fun toString(v: Int): String {
		return v.toString()
	}
	
	fun toString(v: Long): String {
		return v.toString()
	}
	
	fun toString(a: BooleanArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: ByteArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: CharArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: ShortArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: IntArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: LongArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: FloatArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: DoubleArray): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(a: Array<*>): String {
		return when {
			a.isEmpty() -> LIST_EMPTY
			else        -> a.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(c: Collection<*>): String {
		return when {
			c.isEmpty() -> LIST_EMPTY
			else        -> c.joinToString(SEQUENCE_SEPARATOR, LIST_OPEN, LIST_CLOSE, transform = this::toString)
		}
	}
	
	fun toString(m: Map<*, *>): String {
		return when {
			m.isEmpty() -> MAP_EMPTY
			else        -> m.map { toString(it.key) + KEY_VALUE_SEPARATOR + toString(it.value) }.joinToString(SEQUENCE_SEPARATOR, MAP_OPEN, MAP_CLOSE)
		}
	}
}