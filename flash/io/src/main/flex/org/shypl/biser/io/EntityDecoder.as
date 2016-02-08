package org.shypl.biser.io {

	import flash.utils.getQualifiedClassName;

	import org.shypl.common.lang.IllegalArgumentException;

	public class EntityDecoder extends TypedDecoder {
		public function EntityDecoder(type:Class) {
			super(type);
		}

		override public function decode(reader:BiserReader):Object {
			var id:int = reader.readInt();

			if (id == -1) {
				return null;
			}

			var entity:Entity = factory(id);

			if (entity == null) {
				throw new IllegalArgumentException("Сan not decode Entity by class " + getQualifiedClassName(_type) + " (Entity class id " + id + ")");
			}

			entity._decode0(reader);

			return entity;
		}

		protected function factory(id:int):Entity {
			return new _type();
		}
	}
}
