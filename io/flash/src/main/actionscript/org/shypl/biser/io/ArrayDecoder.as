package org.shypl.biser.io {
	import flash.utils.Dictionary;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	
	import org.shypl.common.util.CollectionUtils;
	
	public class ArrayDecoder implements Decoder {
		private static const cache:Dictionary = new Dictionary();
		
		public static function factory(elementDecoder:Decoder):Decoder {
			var arrayDecoder:Decoder = cache[elementDecoder];
			if (arrayDecoder == null) {
				arrayDecoder = new ArrayDecoder(elementDecoder);
				cache[elementDecoder] = arrayDecoder;
			}
			return arrayDecoder;
		}
		
		private var _elementDecoder:Decoder;
		private var _vectorClass:Class;
		
		public function ArrayDecoder(elementDecoder:Decoder) {
			_elementDecoder = elementDecoder;
		}
		
		public function createVector(size:int):Object {
			if (_vectorClass == null) {
				defineVectorClass();
			}
			return new _vectorClass(size, true);
		}
		
		public function decode(reader:DataReader):Object {
			return reader.readArray(_elementDecoder);
		}
		
		private function defineVectorClass():void {
			_vectorClass =
				getDefinitionByName(CollectionUtils.VECTOR_CLASS_NAME + ".<" + getQualifiedClassName(_elementDecoder.createVector(0)) + ">") as Class;
		}
	}
}
