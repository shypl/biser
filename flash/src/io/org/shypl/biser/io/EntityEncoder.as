package org.shypl.biser.io {
	public final class EntityEncoder implements Encoder {
		public static const INSTANCE:Encoder = new EntityEncoder();

		public function encode(value:Object, writer:BiserWriter):void {
			writer.writeEntity(Entity(value));
		}
	}
}
