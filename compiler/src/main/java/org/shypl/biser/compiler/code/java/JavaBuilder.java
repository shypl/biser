package org.shypl.biser.compiler.code.java;

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
import org.shypl.biser.compiler.prototype.Package;
import org.shypl.biser.compiler.prototype.Property;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Set;

public class JavaBuilder extends CodeBuilder
{
	private int notifierId = 0;

	public JavaBuilder(Path path)
	{
		super(path);
	}

	@Override
	public void build(org.shypl.biser.compiler.prototype.Class cls) throws IOException
	{
		JavaFile file = new JavaFile(cls.pkg.fullName());
		CodeClass codeClass;

		if (cls instanceof ObjectDataClass) {
			codeClass = buildObject(file, (ObjectDataClass)cls);
		}
		else if (cls instanceof EnumDataClass) {
			codeClass = buildEnum((EnumDataClass)cls);
		}
		else if (cls instanceof ApiClass) {
			codeClass = buildApi(file, (ApiClass)cls);
		}
		else {
			throw new RuntimeException();
		}

		file.setClass(codeClass);
		file.save(path);
	}

	@Override
	public void buildApiController(org.shypl.biser.compiler.prototype.Package pkg, Set<ApiClass> classes)
		throws IOException
	{
		buildApiConnection(pkg, classes);

		Class controller = new Class(pkg, "AbstractApiController") {};

		JavaFile file = new JavaFile(pkg.fullName());
		file.addImport("org.shypl.biser.api.AbstractController");
		file.addImport("org.shypl.biser.api.Logger");
		file.addImport("org.shypl.biser.OutputBuffer");
		file.addImport("org.shypl.biser.InputBuffer");

		JavaClass codeClass = new JavaClass(controller.name + "<C extends AbstractApiConnection>",
			"AbstractController<C>", Mod.at(Mod.PUBLIC));

		JavaMethod constructor = new JavaMethod(controller.name, null, Mod.at(Mod.PROTECTED));
		codeClass.addMethod(constructor);
//		constructor.addArgument("logger", "Logger");
//		constructor.body.line("super(logger);");

		// services
		JavaMethod route = new JavaMethod("route", "OutputBuffer", Mod.at(Mod.PROTECTED | Mod.OVERRIDE));
		codeClass.addMethod(route);
		route.addThrow("Exception");
		route.addArgument("connection", "C");
		route.addArgument("service", "int");
		route.addArgument("method", "int");
		route.addArgument("input", "InputBuffer");

		route.body.line("OutputBuffer output = null;");
		route.body.line("switch (service) {");

		int i = 0;
		for (ApiClass cls : classes) {
			if (cls.isService()) {
				String name = defineClassName(file, controller, cls);
				String propertyName = cls.name + "Service";
				String typeName = name + ".Service<C>";

				codeClass.addProperty(new JavaProperty(propertyName, typeName, Mod.at(Mod.PRIVATE | Mod.FINAL)));
				constructor.addArgument(propertyName, typeName);
				constructor.body.line("this.", propertyName, " = ", propertyName, ";");

				/// route
				route.body.line(1, "case ", String.valueOf(i++), ":");
				route.body.line(2, "switch (method) {");

				int m = 0;
				for (Method service : cls.serviceMethods) {

					LinkedList<String> debug = new LinkedList<>();
					LinkedList<String> debugArgs = new LinkedList<>();
					LinkedList<String> args1 = new LinkedList<>();
					LinkedList<String> args2 = new LinkedList<>();
					args1.add("connection");
					args2.add("connection");
					int a = 0;

					route.body.line(3, "case ", String.valueOf(m++), ":");

					if (service.hasResult()) {
						route.body.line(4, "output = new OutputBuffer();");
						route.body.line(4, "output.writeInt(input.readInt());");
					}

					route.body.line(4, "if (logger.isTraceEnabled()) {");

					for (Property property : service.properties) {
						String arg = "arg" + (a++);
						args2.add(buildDecodeData(file, controller, codeClass, property.dataType, "input"));
						if (property.dataType == DataType.Primitive.BYTES) {
							file.addImport("org.shypl.biser.BtpUtils");
							debugArgs.add("BtpUtils.convertBytesToHex(" + arg + ")");
						}
						else if (property.dataType instanceof DataType.Array) {
							file.addImport("java.util.Arrays");
							debugArgs.add("Arrays.toString(" + arg + ")");
						}
						else {
							debugArgs.add(arg);
						}
						args1.add(arg);
						debug.add(property.name + ": {}");
						route.body
							.line(5, "final ", defineDataTypeName(file, controller, property.dataType), " ", arg, " = ",
								args2.getLast(), ";");
					}
					if (debugArgs.isEmpty()) {
						route.body.line(5, "logger.trace(\"> ", cls.name, ".", service.name, "()\");");
					}
					else {
						route.body
							.line(5, "logger.trace(\"> ", cls.name, ".", service.name, "(", Utils.join(debug, ", "),
								")\", ", Utils.join(debugArgs, ", "), ");");
					}

					String call = propertyName + "." + service.name + "(" + Utils.join(args1, ", ") + ")";

					if (service.hasResult()) {
						route.body
							.line(5, "final ", defineDataTypeName(file, controller, service.result), " r = ", call,
								";");
						if (service.result == DataType.Primitive.BYTES) {
							file.addImport("org.shypl.biser.BtpUtils");
							route.body.line(5, "logger.trace(\"<< ", cls.name, ".", service.name,
								": {}\", BtpUtils.convertBytesToHex(r));");
						}
						else if (service.result instanceof DataType.Array) {
							file.addImport("java.util.Arrays");
							route.body.line(5, "logger.trace(\"<< ", cls.name, ".", service.name,
								": {}\", Arrays.toString(r));");
						}
						else {
							route.body.line(5, "logger.trace(\"<< ", cls.name, ".", service.name, ": {}\", r);");
						}
						route.body
							.line(5, buildEncodeData(file, controller, codeClass, service.result, "r", "output"), ";");
					}
					else {
						route.body.line(5, call, ";");
					}

					route.body.line(4, "}");
					route.body.line(4, "else {");

					call = propertyName + "." + service.name + "(" + Utils.join(args2, ", ") + ")";

					if (service.hasResult()) {
						route.body
							.line(5, buildEncodeData(file, controller, codeClass, service.result, call, "output"), ";");
					}
					else {
						route.body.line(5, call, ";");
					}

					route.body.line(4, "}");
					route.body.line(4, "break;");
				}

				route.body.line(3, "default:");
				route.body
					.line(4, "throw new RuntimeException(\"Invalid service method \" + service + \".\" + method);");

				route.body.line(2, "}");
				route.body.line(2, "break;");
			}
		}

		route.body.line(1, "default:");
		route.body.line(2, "throw new RuntimeException(\"Invalid service \" + service);");
		route.body.line("}");
		route.body.line("return output;");

		/*
		// agents
		i = 0;
		for (ApiClass cls : classes) {
			if (cls.isNotifier()) {
				String name = defineClassName(file, controller, cls);
				String typeName = name + ".Notifier";
				String propertyName = "agent" + name;

				codeClass.addProperty(new JavaProperty(propertyName, typeName, Mod.at(Mod.PUBLIC | Mod.FINAL)));
				constructor.body.line(propertyName, " = new ", typeName, "(", String.valueOf(i++), ", this);");
			}
		}
		*/

		file.setClass(codeClass);
		file.save(path);
	}

	private void buildApiConnection(final Package pkg, final Set<ApiClass> classes) throws IOException
	{
		Class connection = new Class(pkg, "AbstractApiConnection") {};

		JavaFile file = new JavaFile(pkg.fullName());
		file.addImport("org.shypl.biser.api.AbstractConnection");
		file.addImport("org.shypl.biser.api.ConnectionChannel");

		JavaClass noticeClass = new JavaClass("Notice", null, Mod.at(Mod.PUBLIC | Mod.FINAL));
		JavaMethod constructor = new JavaMethod("Notice", null, Mod.at(Mod.PROTECTED));
		noticeClass.addMethod(constructor);

		boolean hasNotice = false;
		for (ApiClass cls : classes) {
			if (cls.isNotifier()) {
				hasNotice = true;
				String typeName = defineClassName(file, connection, cls) + ".Notifier";
				noticeClass.addProperty(new JavaProperty(cls.name, typeName, Mod.at(Mod.PUBLIC | Mod.FINAL)));
				constructor.body.line(cls.name, " = new ", typeName, "(AbstractApiConnection.this);");
			}
		}

		JavaClass connectionClass = new JavaClass(connection.name + "<C extends ConnectionChannel>", "AbstractConnection<C>",
			Mod.at(Mod.PUBLIC | Mod.ABSTRACT));
		constructor = new JavaMethod(connection.name, null, Mod.at(Mod.PROTECTED));
		connectionClass.addMethod(constructor);
		constructor.addArgument("channel", "C");

		constructor.body.line("super(channel);");

		if (hasNotice) {
			connectionClass.addProperty(new JavaProperty("notice", "Notice", Mod.at(Mod.PUBLIC | Mod.FINAL)));
			constructor.body.line("notice = new Notice();");
			connectionClass.addInnerClass(noticeClass);
		}

		file.setClass(connectionClass);
		file.save(path);
	}

	private CodeClass buildApi(JavaFile file, ApiClass cls)
	{
		JavaClass codeClass = new JavaClass(Utils.toCamelCase(cls.name, true), null, Mod.at(Mod.PUBLIC));

		if (cls.isService()) {
			codeClass.addInnerClass(buildApiService(file, cls));
		}

		if (cls.isNotifier()) {
			codeClass.addInnerClass(buildApiNotifier(file, cls));
		}

		for (DataClass innerClass : cls.innerClasses) {
			final CodeClass inner;
			if (innerClass instanceof ObjectDataClass) {
				inner = buildObject(file, (ObjectDataClass)innerClass);
			}
			else {
				inner = buildEnum((EnumDataClass)innerClass);
			}
			inner.mod.add(Mod.STATIC);
			codeClass.addInnerClass(inner);
		}

		return codeClass;
	}

	private CodeClass buildApiNotifier(JavaFile file, ApiClass cls)
	{
		file.addImport("org.shypl.biser.api.AbstractNotifier");
		file.addImport("org.shypl.biser.api.AbstractConnection");
		file.addImport("org.shypl.biser.OutputBuffer");

		JavaClass codeClass = new JavaClass("Notifier", "AbstractNotifier", Mod.at(Mod.STATIC | Mod.PUBLIC));

		JavaMethod method = new JavaMethod("Notifier", null, Mod.at(Mod.PUBLIC));
		codeClass.addMethod(method);
		method.addArgument("connection", "AbstractConnection");
		method.body.line("super(", String.valueOf(notifierId), ", connection);");

		int i = 0;
		for (Method agent : cls.notifierMethods) {
			method = new JavaMethod(agent.name, "void", Mod.at(Mod.PUBLIC));
			codeClass.addMethod(method);

			LinkedList<String> debug1 = new LinkedList<>();
			LinkedList<String> debug2 = new LinkedList<>();
			for (Property property : agent.properties) {
				debug1.add(property.name + ": {}");
				debug2.add(property.name);
			}
			method.body.line("_trace(\"< ", cls.name, ".", agent.name, "(", Utils.join(debug1, ", "), ")\", ",
				Utils.join(debug2, ", "), ");");

			method.body.line("final OutputBuffer b = new OutputBuffer();");
			method.body.line("b.writeInt(0);");
			method.body.line("b.writeInt(_id);");
			method.body.line("b.writeInt(", String.valueOf(i++), ");");
			for (Property property : agent.properties) {
				method.addArgument(property.name, defineDataTypeName(file, cls, property.dataType));
				method.body.line(buildEncodeData(file, cls, codeClass, property.dataType, property.name, "b"), ";");
			}
			method.body.line("_send(b);");
		}

		++notifierId;

		return codeClass;
	}

	private CodeClass buildApiService(JavaFile file, ApiClass cls)
	{
		file.addImport("org.shypl.biser.api.AbstractConnection");

		JavaClass codeClass = new JavaClass("Service<C extends AbstractConnection>", null,
			Mod.at(Mod.STATIC | Mod.PUBLIC | Mod.INTERFACE));

		for (Method service : cls.serviceMethods) {

			JavaMethod method = new JavaMethod(service.name,
				service.hasResult() ? defineDataTypeName(file, cls, service.result) : "void",
				Mod.at(Mod.PUBLIC | Mod.ABSTRACT));
			codeClass.addMethod(method);

			method.addThrow("Exception");
			method.addArgument("connection", "C");

			for (Property property : service.properties) {
				method.addArgument(property.name, defineDataTypeName(file, cls, property.dataType));
			}
		}

		return codeClass;
	}

	private CodeClass buildObject(JavaFile file, ObjectDataClass cls)
	{
		JavaClass codeClass = new JavaClass(cls.name, defineParentClassName(file, cls), Mod.at(Mod.PUBLIC));

		JavaMethod encode = new JavaMethod("encode", "void", Mod.at(Mod.PUBLIC | Mod.OVERRIDE));
		JavaMethod decode = new JavaMethod("decode", "void", Mod.at(Mod.PUBLIC | Mod.OVERRIDE));

		if (cls.hasParent()) {
			encode.body.line("super.encode(b);");
			decode.body.line("super.decode(b);");
		}

		file.addImport("org.shypl.biser.InputBuffer");
		file.addImport("org.shypl.biser.OutputBuffer");

		encode.addArgument("b", "OutputBuffer");
		decode.addArgument("b", "InputBuffer");

		JavaMethod factory = new JavaMethod("factory", cls.name, Mod.at(Mod.PUBLIC | Mod.STATIC));
		codeClass.addMethod(factory);
		factory.addArgument("f", "int");
		factory.addArgument("b", "InputBuffer");
		factory.body.line("if (f == 0) {");
		factory.body.line(1, "return null;");
		factory.body.line("}");
		factory.body.line("final ", cls.name, " v = new ", cls.name, "();");
		factory.body.line("v.decode(b);");
		factory.body.line("return v;");

		JavaMethod constructor = new JavaMethod(cls.name, null, Mod.at(Mod.PUBLIC),
			new Lines("@SuppressWarnings(\"UnusedDeclaration\")"));
		codeClass.addMethod(constructor);

		boolean hasAltConstructor = false;
		constructor = new JavaMethod(cls.name, null, Mod.at(Mod.PUBLIC),
			new Lines("@SuppressWarnings(\"UnusedDeclaration\")"));

		{
			LinkedList<ObjectDataClass> parents = new LinkedList<>();
			ObjectDataClass parent = cls;

			while (parent.hasParent()) {
				parent = cls.getParent();
				parents.addFirst(parent);
			}

			for (ObjectDataClass parent1 : parents) {
				for (Property property : parent1.properties) {
					hasAltConstructor = true;
					constructor.addArgument(property.name, defineDataTypeName(file, cls, property.dataType));
				}
			}

			if (hasAltConstructor) {
				constructor.body.line("super(", Utils.join(constructor.arguments.keySet(), ", "), ");");
			}
		}

		hasAltConstructor = hasAltConstructor || !cls.properties.isEmpty();

		if (hasAltConstructor) {
			codeClass.addMethod(constructor);
		}

		for (Property property : cls.properties) {
			String type = defineDataTypeName(file, cls, property.dataType);

			constructor.addArgument(property.name, type);
			constructor.body.line("this.", property.name, " = ", property.name, ";");

			codeClass.addProperty(new JavaProperty(property.name, type, Mod.at(Mod.PUBLIC)));

			encode.body.line(buildEncode(file, cls, codeClass, property, "b"), ";");
			decode.body.line(buildDecode(file, cls, codeClass, property, "b"), ";");
		}

		codeClass.addMethod(encode);
		codeClass.addMethod(decode);

		for (DataClass innerClass : cls.innerClasses) {
			final CodeClass inner;
			if (innerClass instanceof ObjectDataClass) {
				inner = buildObject(file, (ObjectDataClass)innerClass);
			}
			else {
				inner = buildEnum((EnumDataClass)innerClass);
			}
			inner.mod.add(Mod.STATIC);
			codeClass.addInnerClass(inner);
		}

		return codeClass;
	}

	private JavaEnumClass buildEnum(EnumDataClass cls)
	{
		return new JavaEnumClass(cls.name, cls.values);
	}

	private String buildDecode(JavaFile file, Class cls, JavaClass codeClass, Property property, String buffer)
	{
		return "this." + property.name + " = " + buildDecodeData(file, cls, codeClass, property.dataType, buffer);
	}

	private String buildDecodeData(JavaFile file, Class cls, JavaClass codeClass, DataType type, String buffer)
	{
		if (type instanceof DataType.Primitive) {
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

			String name = codeClass.getDecoder(type);
			if (name == null) {
				name = codeClass.addDecoder(type);

				DataType subType = ((DataType.Array)type).type;

				if (subType instanceof DataType.Primitive) {
					return buffer + "." + defineBufferMethod(subType, true) + "Array()";
				}

				JavaMethod method = new JavaMethod(name, typeName, Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "InputBuffer");

				method.body
					.line("final ", typeName, " v = new ", typeName.replaceFirst("\\[\\]", "[b.readInt()]"), ";");
				method.body.line("for (int i = 0; i < v.length; ++i) {");
				method.body.line("\tv[i] = ", buildDecodeData(file, cls, codeClass, subType, "b"), ";");
				method.body.line("}");
				method.body.line("return v;");
			}

			return "this." + name + "(" + buffer + ")";
		}

		if (type instanceof DataType.List) {
			file.addImport("java.util.LinkedList");

			String name = codeClass.getDecoder(type);
			if (name == null) {
				name = codeClass.addDecoder(type);

				DataType subType = ((DataType.List)type).type;

				JavaMethod method = new JavaMethod(name, typeName, Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "InputBuffer");

				method.body.line("final ", typeName, " v = new LinkedList<>();");
				method.body.line("for (int i = 0, l = b.readInt(); i < l; ++i) {");
				method.body.line("\tv.add(", buildDecodeData(file, cls, codeClass, subType, "b"), ");");
				method.body.line("}");
				method.body.line("return v;");
			}

			return "this." + name + "(" + buffer + ")";
		}

		if (type instanceof DataType.Map) {
			file.addImport("java.util.LinkedHashMap");

			String name = codeClass.getDecoder(type);
			if (name == null) {
				name = codeClass.addDecoder(type);

				DataType keyType = ((DataType.Map)type).keyType;
				DataType valueType = ((DataType.Map)type).valueType;

				JavaMethod method = new JavaMethod(name, typeName, Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "InputBuffer");

				method.body.line("int l = b.readInt();");
				method.body.line("final ", typeName, " v = new LinkedHashMap<>(l);");
				method.body.line("for (int i = 0; i < l; ++i) {");
				method.body.line("\tv.put(", buildDecodeData(file, cls, codeClass, keyType, "b"), ", ",
					buildDecodeData(file, cls, codeClass, valueType, "b"), ");");
				method.body.line("}");
				method.body.line("return v;");
			}

			return "this." + name + "(" + buffer + ")";
		}

		throw new RuntimeException();
	}

	private String buildEncode(JavaFile file, Class cls, JavaClass codeClass, Property property, String buffer)
	{
		return buildEncodeData(file, cls, codeClass, property.dataType, "this." + property.name, buffer);
	}

	private String buildEncodeData(JavaFile file, Class cls, JavaClass codeClass, DataType type, String data,
		String buffer)
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

			String name = codeClass.getEncoder(type);
			if (name == null) {
				name = codeClass.addEncoder(type);
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

				JavaMethod method = new JavaMethod(name, "void", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "OutputBuffer");
				method.addArgument("v", defineDataTypeName(file, cls, type));

				method.body.line("b.writeInt(v.length);");
				method.body.line("for (", defineDataTypeName(file, cls, subType), " e : v) {");
				method.body.line(1, buildEncodeData(file, cls, codeClass, subType, "e", "b"), ";");
				method.body.line("}");
			}

			return "this." + name + "(" + buffer + ", " + data + ")";
		}

		if (type instanceof DataType.List) {

			String name = codeClass.getEncoder(type);

			if (name == null) {
				name = codeClass.addEncoder(type);

				DataType subType = ((DataType.List)type).type;
				JavaMethod method = new JavaMethod(name, "void", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "OutputBuffer");
				method.addArgument("v", defineDataTypeName(file, cls, type));

				method.body.line("b.writeInt(v.size());");
				method.body.line("for (", defineDataTypeName(file, cls, subType), " e : v) {");
				method.body.line(1, buildEncodeData(file, cls, codeClass, subType, "e", "b"), ";");
				method.body.line("}");
			}

			return "this." + name + "(" + buffer + ", " + data + ")";
		}

		if (type instanceof DataType.Map) {

			String name = codeClass.getEncoder(type);

			if (name == null) {
				name = codeClass.addEncoder(type);

				DataType keyType = ((DataType.Map)type).keyType;
				DataType valueType = ((DataType.Map)type).valueType;

				JavaMethod method = new JavaMethod(name, "void", Mod.at(Mod.PRIVATE));

				codeClass.addMethod(method);
				method.addArgument("b", "OutputBuffer");
				method.addArgument("v", defineDataTypeName(file, cls, type));

				method.body.line("b.writeInt(v.size());");
				method.body.line("for (Map.Entry<", defineDataTypeName(file, cls, keyType, true), ", ",
					defineDataTypeName(file, cls, valueType, true), "> e : v.entrySet()) {");
				method.body.line(1, buildEncodeData(file, cls, codeClass, keyType, "e.getKey()", "b"), ";");
				method.body.line(1, buildEncodeData(file, cls, codeClass, valueType, "e.getValue()", "b"), ";");
				method.body.line("}");
			}

			return "this." + name + "(" + buffer + ", " + data + ")";
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

	private String defineParentClassName(JavaFile file, Class cls)
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

	private String defineDataTypeName(JavaFile file, Class cls, DataType type)
	{
		return defineDataTypeName(file, cls, type, false);
	}

	private String defineDataTypeName(JavaFile file, Class cls, DataType type, boolean primitiveAsObject)
	{
		if (type instanceof DataType.Primitive) {
			if (type == DataType.Primitive.BOOL) {
				return primitiveAsObject ? "Boolean" : "boolean";
			}
			if (type == DataType.Primitive.BYTE) {
				return primitiveAsObject ? "Byte" : "byte";
			}
			if (type == DataType.Primitive.SHORT) {
				return primitiveAsObject ? "Short" : "short";
			}
			if (type == DataType.Primitive.INT) {
				return primitiveAsObject ? "Integer" : "int";
			}
			if (type == DataType.Primitive.UINT) {
				return primitiveAsObject ? "Long" : "long";
			}
			if (type == DataType.Primitive.DOUBLE) {
				return primitiveAsObject ? "Double" : "double";
			}
			if (type == DataType.Primitive.STRING) {
				return "String";
			}
			if (type == DataType.Primitive.BYTES) {
				return "byte[]";
			}
			throw new RuntimeException();
		}

		if (type instanceof DataType.Data) {
			return defineClassName(file, cls, ((DataType.Data)type).cls());
		}

		if (type instanceof DataType.Array) {
			return defineDataTypeName(file, cls, ((DataType.Array)type).type) + "[]";
		}

		if (type instanceof DataType.List) {
			file.addImport("java.util.List");
			return "List<" + defineDataTypeName(file, cls, ((DataType.List)type).type, true) + ">";
		}

		if (type instanceof DataType.Map) {
			file.addImport("java.util.Map");
			return "Map<"
				+ defineDataTypeName(file, cls, ((DataType.Map)type).keyType, true)
				+ ", "
				+ defineDataTypeName(file, cls, ((DataType.Map)type).valueType, true)
				+ ">";
		}

		throw new RuntimeException();
	}

	private String defineClassName(JavaFile file, Class cls, Class target)
	{
		if (cls.pkg == target.pkg) {
			if (target instanceof DataClass) {
				if (((DataClass)target).scope != cls) {
					return target.scopeName(".");
				}
				if (cls instanceof DataClass && ((DataClass)target).scope != ((DataClass)cls).scope) {
					return target.scopeName(".");
				}
			}

			return target.className();
		}

		if (target instanceof DataClass) {
			file.addImport(target.pkg.fullName() + "." + ((DataClass)target).scopeRoot().className());
			return target.scopeName(".");
		}

		file.addImport(target.pkg.fullName() + "." + target.className());
		return target.className();
	}
}
