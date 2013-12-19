package org.shypl.biser.compiler.code.flex;

import org.shypl.biser.compiler.Utils;
import org.shypl.biser.compiler.code.CodeBuilder;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.Lines;
import org.shypl.biser.compiler.code.Mod;
import org.shypl.biser.compiler.prototype.ApiClass;
import org.shypl.biser.compiler.prototype.Class;
import org.shypl.biser.compiler.prototype.DataClass;
import org.shypl.biser.compiler.prototype.DataType;
import org.shypl.biser.compiler.prototype.EnumDataClass;
import org.shypl.biser.compiler.prototype.Method;
import org.shypl.biser.compiler.prototype.ObjectDataClass;
import org.shypl.biser.compiler.prototype.Property;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Set;

public class FlexBuilder extends CodeBuilder
{
	public FlexBuilder(Path path)
	{
		super(path);
	}

	@Override
	public void build(org.shypl.biser.compiler.prototype.Class cls) throws IOException
	{
		if (cls instanceof ApiClass) {
			buildApi((ApiClass)cls);
		}
		else {
			FlexFile file = new FlexFile(cls.pkg.fullName());
			CodeClass codeClass;

			if (cls instanceof ObjectDataClass) {
				codeClass = buildObject(file, (ObjectDataClass)cls);
			}
			else if (cls instanceof EnumDataClass) {
				codeClass = buildEnum(file, (EnumDataClass)cls);
			}
			else {
				throw new RuntimeException();
			}

			file.setClass(codeClass);
			file.save(path);
		}
	}

	@Override
	public void buildApiController(org.shypl.biser.compiler.prototype.Package pkg, Set<ApiClass> classes) throws IOException
	{
		Class controller = new Class(pkg, "AbstractApiController") {};

		FlexFile file = new FlexFile(pkg.fullName());
		file.addImport("org.shypl.biser.api.Controller");
		file.addImport("org.shypl.biser.api.IConnection");
		file.addImport("org.shypl.biser.InputBuffer");

		FlexClass codeClass = new FlexClass(controller.name, "Controller", Mod.at(Mod.PUBLIC | Mod.ABSTRACT));

		FlexMethod constructor = new FlexMethod(controller.name, null, Mod.at(Mod.PUBLIC));
		codeClass.addMethod(constructor);
		constructor.addArgument("connection", "IConnection");
		constructor.body.line("super(connection);");

		int i = 0;
		for (ApiClass cls : classes) {
			if (cls.isService()) {
				String typeName = "Service" + cls.className();
				String propertyName = '_' + cls.name;

				file.addImport(cls.pkg.fullName() + "." + typeName);

				codeClass.addProperty(new FlexProperty(propertyName, typeName, Mod.at(Mod.PRIVATE)));
				constructor.body.line(propertyName, " = new ", typeName, "(", String.valueOf(i++), ", this);");

				FlexMethod getter = new FlexMethod(cls.name, typeName, Mod.at(Mod.PUBLIC | Mod.GETTER));
				getter.body.line("return ", propertyName, ";");
				codeClass.addMethod(getter);
			}
		}

		//

		FlexMethod callNotifier = new FlexMethod("_callNotifier", "void", Mod.at(Mod.PROTECTED | Mod.FINAL | Mod.OVERRIDE));
		callNotifier.addArgument("notifier", "int");
		callNotifier.addArgument("method", "int");
		callNotifier.addArgument("buffer", "InputBuffer");

		callNotifier.body.line("switch (notifier) {");

		i = 0;
		for (ApiClass cls : classes) {
			if (cls.isNotifier()) {
				String name = cls.className();
				String argName = "notifier" + name;
				String propertyName = "_notifier" + name;
				String typeName = "INotifier" + name;

				file.addImport(cls.pkg.fullName() + "." + typeName);

				codeClass.addProperty(new FlexProperty(propertyName, typeName, Mod.at(Mod.PRIVATE)));
				constructor.addArgument(argName, typeName);
				constructor.body.line(propertyName, " = ", argName, ";");

				/// callNotifier
				callNotifier.body.line(1, "case ", String.valueOf(i++), ":");
				callNotifier.body.line(2, "switch (method) {");

				int m = 0;
				for (Method notifier : cls.notifierMethods) {
					callNotifier.body.line(3, "case ", String.valueOf(m++), ":");

					if (notifier.properties.isEmpty()) {
						callNotifier.body.line(4, "_trace(\"< ", cls.name, ".", notifier.name, "()\")");
						callNotifier.body.line(4, propertyName, ".", notifier.name, "();");
					}
					else {
						FlexMethod method = new FlexMethod(propertyName + "_" + notifier.name, "void", Mod.at(Mod.PRIVATE));
						codeClass.addMethod(method);
						method.addArgument("buffer", "InputBuffer");

						LinkedList<String> debug = new LinkedList<>();
						LinkedList<String> args = new LinkedList<>();

						int a = 0;
						for (Property property : notifier.properties) {
							String arg = "arg" + (a++);
							args.add(arg);
							debug.add(property.name + ": {}");
							method.body.line("const ", arg, ":",
								defineDataTypeName(file, controller, property.dataType), " = ",
								buildDecodeData(file, controller, codeClass, property.dataType, "buffer"), ";");
						}
						method.body.line("_trace(\"< ", cls.name, ".", notifier.name, "(", Utils.join(debug, ", "),
							")\", ", Utils.join(args, ", "), ");");
						method.body.line(propertyName, ".", notifier.name, "(", Utils.join(args, ", "), ");");

						callNotifier.body.line(4, method.name, "(buffer);");
					}
					callNotifier.body.line(4, "break;");
				}

				callNotifier.body.line(2, "}");
				callNotifier.body.line(2, "break;");
			}
		}

		callNotifier.body.line(1, "default:");
		callNotifier.body.line(2, "break;");
		callNotifier.body.line("}");

		codeClass.addMethod(callNotifier);

		file.setClass(codeClass);
		file.save(path);
	}

	private void buildApi(ApiClass cls) throws IOException
	{
		for (DataClass innerClass : cls.innerClasses) {
			build(innerClass);
		}

		if (cls.isService()) {
			buildApiService(cls);
		}

		if (cls.isNotifier()) {
			buildApiNotifier(cls);
		}
	}

	private void buildApiNotifier(ApiClass cls) throws IOException
	{
		FlexFile file = new FlexFile(cls.pkg.fullName());

		FlexClass codeClass = new FlexClass("INotifier" + cls.className(), null, Mod.at(Mod.PUBLIC | Mod.INTERFACE));

		for (Method notifier : cls.notifierMethods) {

			FlexMethod method = new FlexMethod(notifier.name, "void", Mod.at(Mod.INTERFACE));
			codeClass.addMethod(method);

			for (Property property : notifier.properties) {
				method.addArgument(property.name, defineDataTypeName(file, cls, property.dataType));
			}

		}

		file.setClass(codeClass);
		file.save(path);
	}

	private void buildApiService(ApiClass cls) throws IOException
	{
		FlexFile file = new FlexFile(cls.pkg.fullName());
		file.addImport("org.shypl.biser.api.Controller");
		file.addImport("org.shypl.biser.api.Service");

		FlexClass codeClass = new FlexClass("Service" + cls.className(), "Service", Mod.at(Mod.PUBLIC));

		FlexMethod constructor = new FlexMethod(codeClass.name, null, Mod.at(Mod.PUBLIC));
		codeClass.addMethod(constructor);
		constructor.addArgument("id", "int");
		constructor.addArgument("controller", "Controller");
		constructor.body.line("super(id, controller);");

		FlexMethod handleResult = new FlexMethod("_handleResult", "void", Mod.at(Mod.PROTECTED | Mod.OVERRIDE));
		handleResult.addArgument("action", "int");
		handleResult.addArgument("handler", "Object");
		handleResult.addArgument("buffer", "InputBuffer");
		handleResult.body.line("switch (action) {");

		boolean hasHandleResult = false;

		int i = 0;
		for (Method service : cls.serviceMethods) {
			FlexMethod method = new FlexMethod(service.name, "void", Mod.at(Mod.PUBLIC));
			codeClass.addMethod(method);

			LinkedList<String> debug1 = new LinkedList<>();
			LinkedList<String> debug2 = new LinkedList<>();
			for (Property property : service.properties) {
				debug1.add(property.name + ": {}");
				debug2.add(property.name);
			}
			if (debug1.isEmpty()) {
				method.body.line("_trace(\"> ", cls.name, ".", service.name, "\");");
			}
			else {
				method.body.line("_trace(\"> ", cls.name, ".", service.name, "(", Utils.join(debug1, ", ") , ")\", ", Utils.join(debug2, ", "),");");
			}

			for (Property property : service.properties) {
				method.addArgument(property.name, defineDataTypeName(file, cls, property.dataType));
			}

			String serviceId = String.valueOf(i++);

			if (service.hasResult()) {
				hasHandleResult = true;

				String handlerName = cls.className() + Utils.toCamelCase(service.name, true);
				FlexClass handlerClass = new FlexClass('I' + handlerName + "Handler", null, Mod.at(Mod.PUBLIC | Mod.INTERFACE));
				FlexMethod handlerMethod = new FlexMethod("handle" + handlerName, "void", Mod.at(Mod.INTERFACE));
				FlexFile handlerFile = new FlexFile(cls.pkg.fullName());
				handlerMethod.addArgument("result", defineDataTypeName(handlerFile, cls, service.result));
				handlerClass.addMethod(handlerMethod);
				handlerFile.setClass(handlerClass);
				handlerFile.save(path);

				file.addImport("org.shypl.biser.InputBuffer");
				method.addArgument("_handler", handlerClass.name);
				String result = defineDataTypeName(file, cls, service.result);
				method.body.line("_writeHandler(", serviceId, ", _handler);");

				FlexMethod handler = new FlexMethod(service.name + "_result", result, Mod.at(Mod.PRIVATE));
				codeClass.addMethod(handler);
				handler.addArgument("buffer", "InputBuffer");
				handler.body.line("var r:", result, " = ",
					buildDecodeData(file, cls, codeClass, service.result, "buffer"), ";");
				handler.body.line("_trace(\"<< ", cls.name, ".", service.name, ": {}\", r);");
				handler.body.line("return r;");

				handleResult.body.line(1, "case ", serviceId, ":");
				handleResult.body.line(2, handlerClass.name, "(handler).", handlerMethod.name, "(", handler.name,
					"(buffer));");
				handleResult.body.line(2, "break;");
			}
			else {
				method.body.line("_buffer.writeInt(", serviceId, ");");
			}

			for (Property property : service.properties) {
				method.body.line(buildEncodeData(file, cls, codeClass, property.dataType, property.name, "_buffer"),
					";");
			}

			method.body.line("_send();");
		}

		if (hasHandleResult) {
			handleResult.body.line(1, "default:");
			handleResult.body.line(2, "throw new Error(\"Illegal action \" + action);");
			handleResult.body.line("}");
			codeClass.addMethod(handleResult);
		}

		file.setClass(codeClass);
		file.save(path);
	}

	private CodeClass buildEnum(FlexFile file, EnumDataClass cls)
	{
		file.addImport("org.shypl.common.lang.Enum");
		return new FlexEnumClass(defineClassName(cls), cls.values);
	}

	private CodeClass buildObject(FlexFile file, ObjectDataClass cls) throws IOException
	{
		FlexClass codeClass = new FlexClass(defineClassName(cls), defineParentClassName(file, cls), Mod.at(Mod.PUBLIC));

		FlexMethod encode = new FlexMethod("encode", "void", Mod.at(Mod.PUBLIC | Mod.OVERRIDE));
		FlexMethod decode = new FlexMethod("decode", "void", Mod.at(Mod.PUBLIC | Mod.OVERRIDE));

		if (cls.hasParent()) {
			encode.body.line("super.encode(b);");
			decode.body.line("super.decode(b);");
		}

		FlexMethod factory = new FlexMethod("factory", codeClass.name, Mod.at(Mod.PUBLIC | Mod.STATIC), new Lines("//noinspection JSUnusedGlobalSymbols"));
		codeClass.addMethod(factory);
		factory.addArgument("f", "int");
		factory.addArgument("b", "InputBuffer");

		factory.body.line("if (f === 0) {");
		factory.body.line(1, "return null;");
		factory.body.line("}");
		factory.body.line("const v:", codeClass.name, " = new ", codeClass.name, "();");
		factory.body.line("v.decode(b);");
		factory.body.line("return v;");

		codeClass.addMethod(encode);
		codeClass.addMethod(decode);

		file.addImport("org.shypl.biser.InputBuffer");
		file.addImport("org.shypl.biser.OutputBuffer");

		encode.addArgument("b", "OutputBuffer");
		decode.addArgument("b", "InputBuffer");

		for (Property property : cls.properties) {
			String type = defineDataTypeName(file, cls, property.dataType);

			codeClass.addProperty(new FlexProperty(property.name, type, Mod.at(Mod.PUBLIC)));

			encode.body.line(buildEncode(file, cls, codeClass, property, "b"), ";");
			decode.body.line(buildDecode(file, cls, codeClass, property, "b"), ";");
		}

		for (DataClass innerClass : cls.innerClasses) {
			build(innerClass);
		}

		return codeClass;
	}

	private String buildEncode(FlexFile file, Class cls, FlexClass codeClass, Property property, String buffer)
	{
		return buildEncodeData(file, cls, codeClass, property.dataType, "this." + property.name, buffer);
	}

	private String buildEncodeData(FlexFile file, Class cls, FlexClass codeClass, DataType type, String data, String buffer)
	{
		if (type instanceof DataType.Primitive) {
			return buffer + "." + defineBufferMethod(type, false) + "(" + data + ")";
		}

		if (type instanceof DataType.Data) {
			DataClass typeClass = ((DataType.Data)type).cls();
			if (typeClass instanceof ObjectDataClass) {
				return buffer + ".writeObject(" + data + ")";
			}
			return buffer + ".writeEnum(" + data + ")";
		}

		if (type instanceof DataType.Array) {

			DataType subType = ((DataType.Array)type).type;

			if (subType instanceof DataType.Primitive) {
				return buffer + "." + defineBufferMethod(subType, false) + "Array(" + data + ")";
			}

			if (subType instanceof DataType.Data) {
				DataClass typeClass = ((DataType.Data)subType).cls();
				if (typeClass instanceof ObjectDataClass) {
					return buffer + ".writeObjectArray(" + data + ")";
				}
				return buffer + ".writeEnumArray(" + data + ")";
			}

			String name = codeClass.getEncoder(type);
			if (name == null) {
				name = codeClass.addEncoder(type);
				FlexMethod method = new FlexMethod(name, "void", Mod.at(Mod.PRIVATE | Mod.STATIC));

				codeClass.addMethod(method);
				method.addArgument("b", "OutputBuffer");
				method.addArgument("v", defineDataTypeName(file, cls, type));
	
				method.body.line("b.writeInt(v.length);");
				method.body.line("for each (var e:", defineDataTypeName(file, cls, subType), " in v) {");
				method.body.line(1, buildEncodeData(file, cls, codeClass, subType, "e", "b"), ";");
				method.body.line("}");
			}

			return name + "(" + buffer + ", " + data + ")";
		}

		if (type instanceof DataType.List) {
			file.addImport("org.shypl.common.collection.IList");
			file.addImport("org.shypl.common.collection.IListIterator");

			String name = codeClass.getEncoder(type);
			if (name == null) {
				name = codeClass.addEncoder(type);

				DataType subType = ((DataType.List)type).type;
				FlexMethod method = new FlexMethod(name, "void", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "OutputBuffer");
				method.addArgument("v", "IList");

				method.body.line("b.writeInt(v.size);");
				method.body.line("const i:IListIterator = v.iterator();");
				method.body.line("while (i.next()) {");
				method.body.line(1, buildEncodeData(file, cls, codeClass, subType,
					defineDataTypeName(file, cls, subType) + "(i.item)", "b"), ";");
				method.body.line("}");
			}

			return "this." + name + "(" + buffer + ", " + data + ")";
		}

		if (type instanceof DataType.Map) {
			file.addImport("org.shypl.common.collection.IMap");
			file.addImport("org.shypl.common.collection.IMapIterator");

			String name = codeClass.getEncoder(type);
			if (name == null) {
				name = codeClass.addEncoder(type);

				DataType keyType = ((DataType.Map)type).keyType;
				DataType valueType = ((DataType.Map)type).valueType;

				FlexMethod method = new FlexMethod(name, "void", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "OutputBuffer");
				method.addArgument("v", "IMap");

				method.body.line("b.writeInt(v.size);");
				method.body.line("const i:IMapIterator = v.iterator();");
				method.body.line("while (i.next()) {");
				method.body.line(1, buildEncodeData(file, cls, codeClass, keyType,
					defineDataTypeName(file, cls, keyType) + "(i.key)", "b"), ";");
				method.body.line(1, buildEncodeData(file, cls, codeClass, keyType,
					defineDataTypeName(file, cls, valueType) + "(i.value)", "b"), ";");
				method.body.line("}");
			}

			return "this." + name + "(" + buffer + ", " + data + ")";
		}

		throw new RuntimeException();
	}

	private String buildDecode(FlexFile file, Class cls, FlexClass codeClass, Property property, String buffer)
	{
		return "this." + property.name + " = " + buildDecodeData(file, cls, codeClass, property.dataType, buffer);
	}

	private String buildDecodeData(FlexFile file, Class cls, FlexClass codeClass, DataType type, String buffer)
	{
		if (type.isPrimitive()) {
			return buffer + "." + defineBufferMethod(type, true) + "()";
		}

		String typeName = defineDataTypeName(file, cls, type);

		if (type instanceof DataType.Data) {

			DataClass typeClass = ((DataType.Data)type).cls();

			if (typeClass instanceof EnumDataClass) {
				return typeName + ".valueOf(" + buffer + ".readInt())";
			}

			return typeName + ".factory(" + buffer + ".readByte(), " + buffer + ")";
		}

		if (type instanceof DataType.Array) {

			DataType subType = ((DataType.Array)type).type;

			if (subType instanceof DataType.Primitive) {
				return buffer + "." + defineBufferMethod(subType, true) + "Array()";
			}

			String name = codeClass.getDecoder(type);
			if (name == null) {
				name = codeClass.addDecoder(type);
				FlexMethod method = new FlexMethod(name, typeName, Mod.at(Mod.PRIVATE | Mod.STATIC));

				codeClass.addMethod(method);
				method.addArgument("b", "InputBuffer");

				method.body.line("var l:int = b.readInt();");
				method.body.line("if (l === -1) {");
				method.body.line(1, "return null;");
				method.body.line("}");
				method.body.line("var v:", typeName, " = new ", typeName, "(l, true);");
				method.body.line("for (var i:int = 0; i < l; ++i) {");
				method.body.line("\tv[i] = ", buildDecodeData(file, cls, codeClass, subType, "b"), ";");
				method.body.line("}");
				method.body.line("return v;");
			}

			return name + "(" + buffer + ")";
		}

		if (type instanceof DataType.List) {
			file.addImport("org.shypl.common.collection.LinkedList");
			file.addImport("org.shypl.common.collection.IList");

			String name = codeClass.getDecoder(type);
			if (name == null) {
				name = codeClass.addDecoder(type);

				DataType subType = ((DataType.List)type).type;

				FlexMethod method = new FlexMethod(name, "IList", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "InputBuffer");

				method.body.line("const v:IList = new LinkedList();");
				method.body.line("const l:int = b.readInt();");
				method.body.line("for (var i:int = 0; i < l; ++i) {");
				method.body.line("\tv.add(", buildDecodeData(file, cls, codeClass, subType, "b"), ");");
				method.body.line("}");
				method.body.line("return v;");
			}

			return "this." + name + "(" + buffer + ")";
		}

		if (type instanceof DataType.Map) {
			file.addImport("org.shypl.common.collection.LinkedMap");
			file.addImport("org.shypl.common.collection.IMap");

			String name = codeClass.getDecoder(type);
			if (name == null) {
				name = codeClass.addDecoder(type);

				DataType keyType = ((DataType.Map)type).keyType;
				DataType valueType = ((DataType.Map)type).valueType;

				FlexMethod method = new FlexMethod(name, "IMap", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "InputBuffer");

				method.body.line("const v:IMap = new LinkedMap();");
				method.body.line("const l:int = b.readInt();");
				method.body.line("for (var i:int = 0; i < l; ++i) {");
				method.body.line("\tv.put(", buildDecodeData(file, cls, codeClass, keyType, "b"), ", ",
					buildDecodeData(file, cls, codeClass, valueType, "b"), ");");
				method.body.line("}");
				method.body.line("return v;");
			}

			return name + "(" + buffer + ")";
		}

		throw new RuntimeException();
	}

	private String defineBufferMethod(DataType type, boolean read)
	{
		String name;

		if (type == DataType.Primitive.BOOL) {
			name = "Bool";
		}
		else if (type == DataType.Primitive.BYTE) {
			name = "Byte";
		}
		else if (type == DataType.Primitive.SHORT) {
			name = "Short";
		}
		else if (type == DataType.Primitive.INT) {
			name = "Int";
		}
		else if (type == DataType.Primitive.UINT) {
			name = "Uint";
		}
		else if (type == DataType.Primitive.DOUBLE) {
			name = "Double";
		}
		else if (type == DataType.Primitive.STRING) {
			name = "String";
		}
		else if (type == DataType.Primitive.BYTES) {
			name = "Bytes";
		}
		else {
			throw new RuntimeException();
		}

		return (read ? "read" : "write") + name;
	}

	private String defineClassName(Class cls)
	{
		return cls.scopeName("");
	}

	private String defineClassName(FlexFile file, Class cls, Class target)
	{
		String name = defineClassName(target);

		if (cls.pkg != target.pkg) {
			file.addImport(target.pkg.fullName() + "." + name);
		}

		return name;
	}

	private String defineParentClassName(FlexFile file, Class cls)
	{
		if (cls instanceof ObjectDataClass) {
			ObjectDataClass c = (ObjectDataClass)cls;
			if (c.hasParent()) {
				return defineClassName(file, cls, c.getParent());
			}
			file.addImport("org.shypl.biser.DataObject");
			return "DataObject";
		}

		return null;
	}

	private String defineDataTypeName(FlexFile file, Class cls, DataType type)
	{
		if (type instanceof DataType.Primitive) {
			if (type == DataType.Primitive.BOOL) {
				return "Boolean";
			}
			if (type == DataType.Primitive.BYTE) {
				return "int";
			}
			if (type == DataType.Primitive.SHORT) {
				return "int";
			}
			if (type == DataType.Primitive.INT) {
				return "int";
			}
			if (type == DataType.Primitive.UINT) {
				return "uint";
			}
			if (type == DataType.Primitive.DOUBLE) {
				return "Number";
			}
			if (type == DataType.Primitive.STRING) {
				return "String";
			}
			if (type == DataType.Primitive.BYTES) {
				file.addImport("flash.utils.ByteArray");
				return "ByteArray";
			}
			throw new RuntimeException();
		}

		if (type instanceof DataType.Data) {
			return defineClassName(file, cls, ((DataType.Data)type).cls());
		}

		if (type instanceof DataType.Array) {
			return "Vector.<" + defineDataTypeName(file, cls, ((DataType.Array)type).type) + ">";
		}

		if (type instanceof DataType.List) {
			file.addImport("org.shypl.common.collection.IList");
			return "IList";
		}

		if (type instanceof DataType.Map) {
			file.addImport("org.shypl.common.collection.IMap");
			return "IMap";
		}

		throw new RuntimeException();
	}
}
