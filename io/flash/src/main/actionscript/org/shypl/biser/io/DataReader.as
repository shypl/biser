package org.shypl.biser.io {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;
	import flash.utils.getQualifiedClassName;
	
	import org.shypl.common.collection.LinkedHashMap;
	import org.shypl.common.collection.Map;
	import org.shypl.common.lang.Enum;
	import org.shypl.common.lang.RuntimeException;
	import org.shypl.common.math.Long;
	
	public class DataReader {
		private var _stream:IDataInput;
		
		public function DataReader(stream:IDataInput) {
			_stream = stream;
		}
		
		public function readByte():int {
			return _stream.readByte();
		}
		
		public function readBool():Boolean {
			return _stream.readByte() === 0x01;
		}
		
		public function readInt():int {
			const b:int = _stream.readUnsignedByte();
			
			switch (b) {
				case 0xFF:
					return -1;
				case 0xFE:
					return _stream.readInt();
				default:
					return b;
			}
		}
		
		public function readUint():uint {
			const b:int = _stream.readUnsignedByte();
			
			switch (b) {
				case 0xFF:
					return _stream.readUnsignedInt();
				default:
					return b;
			}
		}
		
		public function readLong():Long {
			const b:int = _stream.readUnsignedByte();
			switch (b) {
				case 0xFF:
					return Long.NEGATIVE_ONE;
				case 0xFE:
					return Long.valueOfBits(_stream.readUnsignedInt(), _stream.readUnsignedInt());
				default:
					return Long.valueOfInt(b);
			}
		}
		
		public function readUlong():Long {
			const b:int = _stream.readUnsignedByte();
			switch (b) {
				case 0xFF:
					return Long.valueOfBits(_stream.readUnsignedInt(), _stream.readUnsignedInt());
				default:
					return Long.valueOfInt(b);
			}
		}
		
		public function readDouble():Number {
			return _stream.readDouble();
		}
		
		public function readBytes():ByteArray {
			const size:int = readInt();
			
			if (size === -1) {
				return null;
			}
			
			const bytes:ByteArray = new ByteArray();
			
			if (size === 0) {
				return bytes;
			}
			
			_stream.readBytes(bytes, 0, size);
			bytes.position = 0;
			
			return bytes;
		}
		
		public function readString():String {
			const size:int = readInt();
			
			if (size === -1) {
				return null;
			}
			
			if (size === 0) {
				return "";
			}
			
			return _stream.readUTFBytes(size);
		}
		
		public function readDate():Date {
			const ms:Long = Long.valueOfBits(_stream.readUnsignedInt(), _stream.readUnsignedInt());
			return ms.equals(Long.MIN_VALUE) ? null : new Date(ms.numberValue());
		}
		
		public function readEnum(type:Class):Enum {
			const ordinal:int = readInt();
			
			if (ordinal === -1) {
				return null;
			}
			
			return Enum.valueOfOrdinal(type, ordinal);
		}
		
		public function readEntity(type:Class):Entity {
			const id:int = readInt();
			
			if (id === -1) {
				return null;
			}
			
			try {
				const entity:Entity = new type();
			}
			catch (e:Error) {
				throw new RuntimeException("Сan not create Entity by class " + getQualifiedClassName(type), e);
			}
			
			if (entity._id !== id) {
				throw new RuntimeException("Сan not create Entity by class " + getQualifiedClassName(type)
					+ " (Class id " + entity._id + " differs from the received " + id + ")");
			}
			
			entity._decode0(this);
			
			return entity;
		}
		
		public function readArray(elementDecoder:Decoder):Object {
			const size:int = readInt();
			
			if (size === -1) {
				return null;
			}
			
			const array:Object = elementDecoder.createVector(size);
			
			for (var i:int = 0; i < size; ++i) {
				array[i] = elementDecoder.decode(this);
			}
			
			return array;
		}
		
		public function readMap(keyDecoder:Decoder, valueDecoder:Decoder):Map {
			const size:int = readInt();
			
			if (size === -1) {
				return null;
			}
			
			const map:Map = new LinkedHashMap();
			
			for (var i:int = 0; i < size; ++i) {
				map.put(keyDecoder.decode(this), valueDecoder.decode(this));
			}
			
			return map;
		}
	}
}
