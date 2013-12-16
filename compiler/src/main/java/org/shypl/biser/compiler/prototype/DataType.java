package org.shypl.biser.compiler.prototype;

public abstract class DataType
{
	public boolean isPrimitive()
	{
		return this instanceof Primitive;
	}

	public static class Primitive extends DataType
	{
		public static final Primitive BOOL   = new Primitive();
		public static final Primitive BYTE   = new Primitive();
		public static final Primitive SHORT  = new Primitive();
		public static final Primitive INT    = new Primitive();
		public static final Primitive UINT   = new Primitive();
		public static final Primitive DOUBLE = new Primitive();
		public static final Primitive STRING = new Primitive();
		public static final Primitive BYTES  = new Primitive();
	}

	public static class Data extends DataType
	{
		private DataClass cls;
		private String    className;
		private Class     scope;

		public Data(String className, Class scope)
		{
			this.className = className;
			this.scope = scope;
		}

		public Data(DataClass cls)
		{
			this.cls = cls;
		}

		public DataClass cls()
		{
			if (cls == null) {
				cls = scope.findInnerClass(className);
				if (cls == null) {
					cls = (DataClass)scope.pkg.findClass(className);
				}
				if (cls == null) {
					cls = (DataClass)scope.pkg.root().findClass(className);
				}

				if (cls == null) {
					throw new RuntimeException("Class not found (" + className + ")");
				}
			}

			return cls;
		}
	}

	public static class Array extends DataType
	{
		public final DataType type;

		public Array(DataType type)
		{
			this.type = type;
		}
	}

	public static class List extends DataType
	{
		public final DataType type;

		public List(DataType type)
		{
			this.type = type;
		}
	}

	public static class Map extends DataType
	{
		public final DataType keyType;
		public final DataType valueType;

		public Map(DataType keyType, DataType valueType)
		{
			this.keyType = keyType;
			this.valueType = valueType;
		}
	}
}
