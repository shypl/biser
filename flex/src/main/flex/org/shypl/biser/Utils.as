package org.shypl.biser
{
	import flash.utils.describeType;

	public final class Utils
	{
		public static function representObject(object:DataObject):String
		{
			var cls:XML = describeType(object);
			var data:Vector.<String> = new Vector.<String>();

			for each(var prop:XML in cls..variable) {
				var name:String = prop.@name;
				var value:* = object[name];
				var type:String = prop.@type;

				if (value !== null && (value is Array || type.indexOf("__AS3__.vec::Vector.") === 0)) {
					data.push(name + ": [" + value + "]");
				}
				else {
					data.push(name + ": " + value);
				}

				//
			}

			return "{" + data.join(", ") + "}";
		}
	}
}
