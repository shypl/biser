package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.Side;
import org.shypl.biser.compiler.Utils;
import org.shypl.biser.compiler.prototype.Entity;
import org.shypl.biser.compiler.prototype.Parameter;
import org.shypl.biser.compiler.prototype.Service;
import org.shypl.biser.compiler.prototype.ServiceMethod;
import org.shypl.biser.compiler.prototype.ServiceMethodResult;
import org.shypl.biser.compiler.prototype.Type;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuilderJava extends Builder
{
	private final Map<ServiceMethod, CodeClass> serverResults     = new HashMap<>();
	private final Set<Service>                  forceSaveServices = new HashSet<>();

	public BuilderJava(final Path path, final String stage, final String pkg, final Side side)
	{
		super(path, stage, pkg, side, "java", "CollectionFactory");
	}

	@Override
	protected void buildApiClient(final Service[] services) throws IOException
	{
		throw new RuntimeException("Not supported");
	}

	@Override
	protected void buildApiServer(final Service[] services) throws IOException
	{
		for (Service service : services) {
			if (service.hasClientMethods()) {
				buildApiServerServiceClient(service);
			}
			if (service.hasServerMethods()) {
				buildApiServerServiceServer(service);
			}
		}

		buildApiServerClient(services);
		buildApiServerServiceRouter(services);

		for (Service service : services) {
			if (service.hasEntities()) {
				for (Entity entity : service.getEntities()) {
					buildEntity(entity);
				}
				save(getClass(service));
			}
			else if (forceSaveServices.contains(service)) {
				save(getClass(service));
			}
		}
	}

	@Override
	protected void buildCode(final CodeClass cls, final Code code)
	{
		buildCode(cls, code, 0);
	}

	@Override
	protected void buildCollectionFactory(final CodeClass cls)
	{
		if (!decodeCollections.isEmpty()) {
			cls.addImport("org.shypl.biser.InputBuffer");
			for (Type type : decodeCollections) {
				final String typeName = defineType(type, cls);
				final CodeMethod method = cls.addMethod("decode" + type.name(), Mod.STATIC | Mod.PUBLIC, typeName);
				final Code body = method.body;

				method.addArgument("buffer", "InputBuffer", true);

				body.line("final int l = buffer.readInt();")
					.line("if (l == -1) {")
					.lineTab(1, "return null;")
					.line("}");

				if (type instanceof Type.List) {
					final Type.List type1 = (Type.List)type;
					if (type1.isArray) {
						body.line("final ", typeName, " r = new ", typeName, "[l];")
							.line("for (var i:int = 0; i != l; ++i) {")
							.lineTab(1, "r[i] = ", defineDecode(type1.type, "buffer", cls), ";");
					}
					else {
						body.line("final ", typeName, " r = new ", typeName, "(l);")
							.line("for (var i:int = 0; i != l; ++i) {")
							.lineTab(1, "r.add(", defineDecode(type1.type, "buffer", cls), ");");
					}
				}
				else {
					final Type.Map type1 = (Type.Map)type;
					body.line("final ", typeName, " r = new ", typeName, "(l);")
						.line("for (var i:int = 0; i != l; ++i) {")
						.lineTab(1, "r.put(", defineDecode(type1.key, "buffer", cls), ", ", defineDecode(type1.value, "buffer", cls), ");");
				}

				body.line("}")
					.line("return r;");
			}
		}

		if (!encodeCollections.isEmpty()) {
			cls.addImport("org.shypl.biser.OutputBuffer");
			for (Type type : encodeCollections) {

				final String typeName = defineType(type, cls);
				final CodeMethod method = cls.addMethod("encode" + type.name(), Mod.STATIC | Mod.PUBLIC, VOID);
				final Code body = method.body;

				method.addArgument("buffer", "OutputBuffer");
				method.addArgument("collection", typeName);

				body.line("if (collection == null) {")
					.lineTab(1, "buffer.writeInt(-1);")
					.line("}");

				if (type instanceof Type.List) {
					final Type.List type1 = (Type.List)type;

					if (type1.isArray) {
						body.line("else if (collection.length == 0) {");
					}
					else {
						body.line("else if (collection.isEmpty()) {");
					}

					body.lineTab(1, "buffer.writeInt(0);")
						.line("}")
						.line("else {");

					if (type1.isArray) {
						body.lineTab(1, "buffer.writeInt(collection.length));");
					}
					else {
						body.lineTab(1, "buffer.writeInt(collection.size());");
					}

					body.lineTab(1, "for (", defineType(type1.type, cls, true), " e : collection) {")
						.lineTab(2, defineEncode("e", type1.type, "buffer", cls), ";")
						.lineTab(1, "}")
						.line("}");
				}
				else {
					final Type.Map type1 = (Type.Map)type;
					cls.addImport("java.util.Map");

					body.line("else if (collection.isEmpty()) {")
						.lineTab(1, "buffer.writeInt(0);")
						.line("}")
						.line("else {")
						.lineTab(1, "buffer.writeInt(collection.size());")
						.lineTab(1, "for (Map.Entry<", defineType(type1.key, cls, true), ", ", defineType(type1.value, cls, true),
							"> e : collection.entrySet()) {")
						.lineTab(2, defineEncode("e.getKey()", type1.key, "buffer", cls), ";")
						.lineTab(2, defineEncode("e.getValue()", type1.value, "buffer", cls), ";")
						.lineTab(1, "}")
						.line("}");
				}
			}
		}
	}

	@Override
	protected void buildEntityBody(final CodeClass cls, final Entity entity)
	{
		final boolean hasParent = entity.hasParent();
		final Parameter[] properties = entity.getProperties();

		// static decode
		if (decodeFactories.contains(entity) || entity.hasDecodeStage(stage)) {
			cls.addImport("org.shypl.biser.InputBuffer");

			final CodeMethod method = cls.addMethod("decodeFactory", Mod.STATIC | Mod.PUBLIC, cls.name);
			method.addArgument("buffer", "InputBuffer");

			method.body.line("final ", cls.name, " object;")
				.line("switch(buffer.readInt()) {");

			for (Entity child : entity.getChildren()) {
				method.body.lineTab(1, "case ", child.getId(), ":")
					.lineTab(2, "object = new ", defineEntityClassName(child), "();")
					.lineTab(2, "break;");
			}

			method.body.lineTab(1, "case -1:")
				.lineTab(2, "return null;")
				.lineTab(1, "default:")
				.lineTab(2, "throw new IllegalArgumentException();")
				.line("}")
				.line("object.decode(buffer);")
				.line("return object;");
		}

		// constructors
		if (encodes.contains(entity)) {
			// empty
			cls.addConstructor()
				.head.add("@SuppressWarnings(\"UnusedDeclaration\")");

			// full
			final LinkedList<Parameter> parentProperties = new LinkedList<>();
			if (entity.hasParent()) {
				Entity e = entity;
				do {
					e = e.getParent();
					final Parameter[] properties1 = e.getProperties();
					for (int i = properties1.length - 1; i >= 0; i--) {
						parentProperties.addFirst(properties1[i]);
					}
				}
				while (e.hasParent());
			}

			CodeMethod constructor = null;

			if (!parentProperties.isEmpty()) {
				constructor = cls.addConstructor();
				final List<String> names = new LinkedList<>();
				for (Parameter property : parentProperties) {
					constructor.addArgument(property.name, defineType(property.type, cls));
					names.add(property.name);
				}
				constructor.body.line("super(", Utils.join(names, ", "), ");");
			}

			if (entity.hasProperties()) {
				if (constructor == null) {
					constructor = cls.addConstructor();
				}
				for (Parameter property : properties) {
					constructor.addArgument(property.name, defineType(property.type, cls));
					constructor.body.line("this.", property.name, " = ", property.name, ";");
				}
			}

			if (constructor != null) {
				constructor.head.add("@SuppressWarnings(\"UnusedDeclaration\")");
			}
		}

		// decode
		if (decodes.contains(entity) || entity.hasDecodeStage(stage)) {
			cls.addImport("org.shypl.biser.InputBuffer");

			final CodeMethod method = cls.addMethod("decode", (hasParent ? Mod.OVERRIDE : 0) | Mod.PUBLIC, VOID);
			method.addArgument("buffer", "InputBuffer");
			if (hasParent) {
				method.body.line("super.decode(buffer);");
			}
			for (Parameter property : properties) {
				method.body.line(property.name, " = ", defineDecode(property.type, "buffer", cls), ";");
			}
		}

		// encode
		if (encodes.contains(entity) || entity.hasEncodeStage(stage)) {
			cls.addImport("org.shypl.biser.OutputBuffer");

			final CodeMethod method = cls.addMethod("encode", Mod.OVERRIDE | Mod.PUBLIC, VOID);
			method.addArgument("buffer", "OutputBuffer");
			if (hasParent) {
				method.body.line("super.encode(buffer);");
			}
			for (Parameter property : properties) {
				method.body.line(defineEncode(property.name, property.type, "buffer", cls), ";");
			}
		}

		// represent
		if (entity.hasProperties()) {
			final CodeMethod method = cls.addMethod("represent", Mod.PROTECTED | Mod.OVERRIDE, VOID);
			cls.addImport("org.shypl.biser.Represent");
			method.addArgument("r", "Represent", true);

			if (entity.hasParent()) {
				method.body.line("super.represent(r);");
			}

			for (Parameter property : properties) {
				method.body.line("r.add(\"" + property.name + "\", this.", property.name, ");");
			}
		}
	}

	@Override
	protected void buildEntityBodyEnum(final CodeClass cls, final Entity entity)
	{
		if (decodeFactories.contains(entity)) {
			cls.addImport("org.shypl.biser.InputBuffer");

			final CodeMethod method = cls.addMethod("decode", Mod.STATIC | Mod.PUBLIC, cls.name);
			method.addArgument("buffer", "InputBuffer");

			method.body.line("switch (buffer.readInt()) {");
			int i = 0;
			for (String value : entity.getEnumValues()) {
				method.body.lineTab(1, "case ", i++, ":")
					.lineTab(2, "return ", value, ";");
			}
			method.body.lineTab(1, "case -1:")
				.lineTab(2, "return null;")
				.lineTab(1, "default:")
				.lineTab(2, "throw new IllegalArgumentException();")
				.line("}");
		}
	}

	@Override
	protected String defineEntityClassName(final Entity entity)
	{
		return entity.name;
	}

	@Override
	protected void defineEntityDependencies(final CodeClass cls, final Entity entity)
	{
		super.defineEntityDependencies(cls, entity);

		if (!entity.isEnum() && !entity.hasParent()) {
			cls.addImport("org.shypl.biser.BiserEntity");
			cls.setParent("BiserEntity");
		}

		if (entity.hasOwner()) {
			getClass(entity.getOwner()).addInner(cls);
		}
	}

	@Override
	protected String defineType(final Type type, final CodeClass cls, final boolean forCollection)
	{
		if (type instanceof Type.Primitive) {
			if (type == Type.Primitive.BOOL) {
				return forCollection ? "Boolean" : "boolean";
			}
			if (type == Type.Primitive.BYTE) {
				return forCollection ? "Byte" : "byte";
			}
			if (type == Type.Primitive.BYTES) {
				return "byte[]";
			}
			if (type == Type.Primitive.INT) {
				return forCollection ? "Integer" : "int";
			}
			if (type == Type.Primitive.UINT) {
				return forCollection ? "Long" : "long";
			}
			if (type == Type.Primitive.NUM) {
				return forCollection ? "Long" : "long";
			}
			if (type == Type.Primitive.DOUBLE) {
				return forCollection ? "Double" : "double";
			}
			if (type == Type.Primitive.STRING) {
				return "String";
			}
			throw new IllegalArgumentException();
		}

		if (type instanceof Type.Entity) {
			return cls.addImport(getClass(((Type.Entity)type).entity())).getNameFor(cls);
		}

		if (type instanceof Type.List) {
			Type.List type1 = (Type.List)type;
			if (type1.isArray) {
				return defineType(type1.type, cls) + "[]";
			}

			cls.addImport("java.util.LinkedList");
			return "LinkedList<" + defineType(type1.type, cls, true) + ">";
		}

		if (type instanceof Type.Map) {
			Type.Map type1 = (Type.Map)type;
			cls.addImport("java.util.LinkedHashMap");
			return "LinkedHashMap<"
				+ defineType(type1.key, cls, true) + ", "
				+ defineType(type1.value, cls, true)
				+ ">";
		}

		throw new IllegalArgumentException();
	}

	@Override
	protected void save(final CodeClass cls) throws IOException
	{
		if (!cls.isInner()) {
			super.save(cls);
		}
	}

	private void buildApiServerClient(final Service[] services) throws IOException
	{
		final CodeClass cls = createClass("Client", Mod.PUBLIC, true);

		cls.addImport("org.shypl.biser.server.AbstractClient");
		cls.addImport("org.shypl.biser.server.ClientConnection");
		cls.addImport("org.shypl.biser.server.ClientController");
		cls.setParent("AbstractClient");

		final CodeMethod constructor = cls.addConstructor();
		constructor.addArgument("connection", "ClientConnection", true);
		constructor.addArgument("controller", "ClientController", true);
		constructor.addArgument("authorized", "boolean", true);
		constructor.body.line("super(connection, controller, authorized);");

		for (Service service : services) {
			if (service.hasClientMethods()) {
				final CodeParameter property = cls.addProperty(service.name, service.getFullName() + "Client", Mod.PUBLIC | Mod.FINAL);
				constructor.body.line(service.name, " = new ", property.type, "(connection);");
			}
		}

		save(cls);
	}

	private void buildApiServerServiceClient(final Service service) throws IOException
	{
		final CodeClass cls = createClass(service.getFullName() + "Client", Mod.PUBLIC | Mod.FINAL, true);
		final CodeMethod constructor = cls.addConstructor();

		cls.addImport("org.shypl.biser.server.ClientConnection");
		cls.addImport("org.shypl.biser.server.ClientApiService");
		cls.setParent("ClientApiService");
		constructor.addArgument("connection", "ClientConnection", true);
		constructor.body.line("super(", service.id, ", connection);");

		for (ServiceMethod serviceMethod : service.getClientMethods()) {
			final CodeMethod method = cls.addMethod(serviceMethod.name, Mod.PUBLIC, VOID);
			if (serviceMethod.hasArguments()) {
				cls.addImport("org.shypl.biser.OutputBuffer");
				method.body.line("final OutputBuffer _m  = _createMessage(", serviceMethod.id, ");");
				for (Parameter arg : serviceMethod.getArguments()) {
					method.addArgument(arg.name, defineType(arg.type, cls), true);
					method.body.line(defineEncode(arg.name, arg.type, "_m", cls), ";");
				}
				method.body.line("_sendMessage(_m);");
			}
			else {
				method.body.line("_sendMessage(_createMessage(", serviceMethod.id, "));");
			}
		}

		save(cls);
	}

	private void buildApiServerServiceRouter(final Service[] services) throws IOException
	{
		final CodeClass cls = createClass("ServiceRouter", Mod.PUBLIC, true);
		cls.addImport("org.shypl.biser.InputBuffer");
		cls.addImport("org.shypl.biser.OutputBuffer");
		cls.addImport("org.shypl.biser.server.AbstractServiceRouter");
		cls.generics.add("C extends Client");
		cls.setParent("AbstractServiceRouter<C>");

		final CodeMethod constructor = cls.addConstructor();
		final CodeMethod route = cls.addMethod("route", Mod.OVERRIDE | Mod.PROTECTED, "OutputBuffer");
		route.throwsList.add("Exception");
		route.addArgument("client", "C", true);
		route.addArgument("service", "int", true);
		route.addArgument("method", "int", true);
		route.addArgument("buffer", "InputBuffer", true);

		route.body.line("switch(service) {");

		for (Service service : services) {
			if (service.hasServerMethods()) {
				final CodeParameter property = cls.addProperty(service.name, service.getFullName() + "Server<C>", Mod.PRIVATE | Mod.FINAL);
				constructor.addArgument(service.name, property.type, true);
				constructor.body.line("this.", property.name, " = ", service.name, ";");
				route.body.lineTab(1, "case ", service.id, ":")
					.lineTab(2, "switch(method) {");
				for (ServiceMethod method : service.getServerMethods()) {

					String call = property.name + "." + method.name + "(client";

					if (method.hasResult() && !(method.getResult() instanceof ServiceMethodResult.Simple)) {
						call += ", new " + serverResults.get(method).getNameFor(cls) + "(buffer.readInt(), client.getConnection())";
					}

					for (Parameter arg : method.getArguments()) {
						call += ", " + defineDecode(arg.type, "buffer", cls);
					}
					call += ")";

					route.body.addTab(3, "case ", method.id, ":");

					if (method.hasResult()) {
						final ServiceMethodResult result = method.getResult();
						if (result instanceof ServiceMethodResult.Simple) {
							final ServiceMethodResult.Simple result1 = (ServiceMethodResult.Simple)result;
							route.body.line("{")
								.lineTab(4, "final OutputBuffer result = new OutputBuffer();")
								.lineTab(4, "result.writeInt(buffer.readInt());")
								.lineTab(4, defineEncode(call, result1.type, "result", cls), ";")
								.lineTab(4, "return result;")
								.lineTab(3, "}");
						}
						else {
							route.body.line()
								.lineTab(4, call, ";")
								.lineTab(4, "return null;");
						}
					}
					else {
						route.body.line()
							.lineTab(4, call, ";")
							.lineTab(4, "return null;");
					}
				}
				route.body.lineTab(2, "}");
			}
		}

		route.body.line("}")
			.line("throw new IllegalArgumentException(\"service: \" + service + \", method: \" + method);");

		save(cls);
	}

	private void buildApiServerServiceServer(final Service service) throws IOException
	{
		final CodeClass cls = createClass(service.getFullName() + "Server", Mod.PUBLIC | Mod.INTERFACE, true);
		cls.generics.add("C extends Client");

		for (ServiceMethod serviceMethod : service.getServerMethods()) {
			final CodeMethod method;

			if (serviceMethod.hasResult()) {
				final ServiceMethodResult methodResult = serviceMethod.getResult();
				if (methodResult instanceof ServiceMethodResult.Simple) {
					method = cls.addMethod(serviceMethod.name, 0, defineType(((ServiceMethodResult.Simple)methodResult).type, cls));
					method.addArgument("client", "C");
				}
				else {
					method = cls.addMethod(serviceMethod.name, 0, VOID);
					method.addArgument("client", "C");
					method.addArgument("backResult", buildApiServerServiceServerResult(service, serviceMethod, methodResult, cls));
				}
			}
			else {
				method = cls.addMethod(serviceMethod.name, 0, VOID);
				method.addArgument("client", "C");
			}

			method.throwsList.add("Exception");

			for (Parameter argument : serviceMethod.getArguments()) {
				method.addArgument(argument.name, defineType(argument.type, cls), true);
			}
		}

		save(cls);
	}

	private String buildApiServerServiceServerResult(final Service service, final ServiceMethod method, final ServiceMethodResult result,
		final CodeClass serviceCls)
	{
		final CodeClass cls = createClass(Utils.toCamelCase(method.name) + "Result", Mod.PUBLIC | Mod.FINAL, true);
		getClass(service).addInner(cls);
		forceSaveServices.add(service);

		cls.addImport("org.shypl.biser.OutputBuffer");
		cls.addImport("org.shypl.biser.server.ClientConnection");
		cls.addImport("org.shypl.biser.server.ServerApiResult");
		cls.setParent("ServerApiResult");

		final CodeMethod constructor = cls.addConstructor();
		constructor.addArgument("id", "int", true);
		constructor.addArgument("connection", "ClientConnection", true);
		constructor.body.line("super(id, connection);");

		final CodeMethod message = cls.addMethod("send", Mod.PUBLIC | Mod.FINAL, VOID);

		if (result instanceof ServiceMethodResult.Lazy) {
			final Type type = ((ServiceMethodResult.Lazy)result).type;
			message.addArgument("result", defineType(type, cls), true);
			message.body.line(defineEncode("result", type, "this.message", cls), ";");
		}
		else {
			for (Parameter param : ((ServiceMethodResult.LazyMulti)result).getParameters()) {
				message.addArgument(param.name, defineType(param.type, cls), true);
				message.body.line(defineEncode(param.name, param.type, "this.message", cls), ";");
			}
		}
		message.body.line("send();");

		serverResults.put(method, cls);

		return cls.getNameFor(serviceCls);
	}

	private void buildCode(final CodeClass cls, final Code code, final int indent)
	{
		final boolean root = indent == 0;
		final int indent1 = indent + 1;
		final int indent2 = indent + 2;

		// head
		if (root) {
			code.line("package ", cls.pkg, ";")
				.line();

			final String[] imports = cls.getImports();
			if (imports.length > 0) {
				for (String value : imports) {
					code.line("import ", value, ";");
				}
				code.line();
			}
		}

		// declare
		code.addTab(indent, cls.getModString(),
			cls.isInterface() ? "interface" : cls.isEnum() ? "enum" : "class", " ", cls.name);

		if (!cls.generics.isEmpty()) {
			code.add("<", Utils.join(cls.generics, ", "), ">");
		}

		if (cls.hasParent()) {
			code.add(" extends ", cls.getParent());
		}
		code.line()
			.lineTab(indent, "{");

		// enum
		boolean addEnumSep = false;
		if (cls.isEnum()) {
			addEnumSep = true;
			final String[] enumValues = cls.getEnumValues();
			for (int i = 0; i < enumValues.length; i++) {
				code.lineTab(indent1, enumValues[i], i == enumValues.length - 1 ? "" : ",");
			}
		}

		// properties
		final CodeParameter[] properties = cls.getProperties();
		if (properties.length > 0) {
			for (CodeParameter property : properties) {
				code.lineTab(indent1, property.getModString(), property.type, " ", property.name, ";");
			}
			code.line();
		}

		// constructors
		for (CodeMethod constructor : cls.getConstructors()) {
			for (String line : constructor.head) {
				code.lineTab(indent1, line);
			}

			code.addTab(indent1, constructor.getModString(), constructor.name, "(");
			final CodeParameter[] arguments = constructor.getArguments();
			for (int i = 0; i < arguments.length; i++) {
				final CodeParameter argument = arguments[i];
				code.add(argument.getModString(), argument.type, " ", argument.name);
				if (i != arguments.length - 1) {
					code.add(", ");
				}
			}
			code.line(")")
				.lineTab(indent1, "{")
				.add(constructor.body.toString(indent2))
				.lineTab(indent1, "}")
				.line();
		}

		// methods
		for (CodeMethod method : cls.getMethods()) {

			if (addEnumSep) {
				code.addTab(indent1, ";").line();
				addEnumSep = false;
			}

			for (String line : method.head) {
				code.lineTab(indent1, line);
			}
			if (method.isOverride()) {
				code.lineTab(indent1, "@Override");
			}
			code.addTab(indent1, method.getModString(), method.result, " ", method.name, "(");

			final CodeParameter[] arguments = method.getArguments();
			for (int i = 0; i < arguments.length; i++) {
				final CodeParameter argument = arguments[i];
				code.add(argument.getModString(), argument.type, " ", argument.name);
				if (i != arguments.length - 1) {
					code.add(", ");
				}
			}

			code.add(")");

			if (!method.throwsList.isEmpty()) {
				code.add(" throws ", Utils.join(method.throwsList, ", "));
			}

			if (cls.isInterface()) {
				code.line(";")
					.line();
			}
			else {
				code.line()
					.lineTab(indent1, "{")
					.add(method.body.toString(indent2))
					.lineTab(indent1, "}")
					.line();
			}
		}

		for (CodeClass inner : cls.getInners()) {
			buildCode(inner, code, 1);
			code.line();
		}

		code.lineTab(indent, "}");
	}

	private String defineBufferMethod(final Type type, final boolean read, final boolean array)
	{
		final String name;

		if (type == Type.Primitive.BOOL) {
			name = "Bool";
		}
		else if (type == Type.Primitive.BYTE) {
			name = "Byte";
		}
		else if (type == Type.Primitive.INT) {
			name = "Int";
		}
		else if (type == Type.Primitive.UINT) {
			name = "Uint";
		}
		else if (type == Type.Primitive.NUM) {
			name = "Num";
		}
		else if (type == Type.Primitive.DOUBLE) {
			name = "Double";
		}
		else if (type == Type.Primitive.STRING) {
			name = "String";
		}
		else if (type == Type.Primitive.BYTES) {
			name = "Bytes";
		}
		else {
			throw new IllegalArgumentException();
		}

		return (read ? "read" : "write") + name + (array ? "Array" : "");
	}

	private String defineDecode(final Type type, final String bufferName, final CodeClass cls)
	{
		if (type instanceof Type.Primitive) {
			return bufferName + "." + defineBufferMethod(type, true, false) + "()";
		}

		defineType(type, cls);

		if (type instanceof Type.Entity) {
			final Entity entity = ((Type.Entity)type).entity();
			decodeFactories.add(entity);

			if (entity.isEnum()) {
				return getClass(entity).getNameFor(cls) + ".decode(" + bufferName + ")";
			}

			decodes.add(entity);
			if (!entity.isEnum()) {
				if (entity.hasParent()) {
					Entity parent = entity.getParent();
					while (parent != null) {
						decodes.add(parent);
						parent = parent.getParent();
					}
				}
				Collections.addAll(decodes, entity.getChildren());
			}
			return getClass(entity).getNameFor(cls) + ".decodeFactory(" + bufferName + ")";
		}

		if (type instanceof Type.List) {
			final Type.List type1 = (Type.List)type;
			if (type1.isArray && type1.type instanceof Type.Primitive) {
				return bufferName + "." + defineBufferMethod(type1.type, true, true) + "()";
			}
		}

		if (type instanceof Type.List) {
			defineDecode(((Type.List)type).type, bufferName, cls);
		}
		else {
			final Type.Map type1 = (Type.Map)type;
			defineDecode(type1.key, bufferName, cls);
			defineDecode(type1.value, bufferName, cls);
		}

		cls.addImport(pkg + '.' + collectionFactory);

		decodeCollections.add(type);
		return collectionFactory + ".decode" + type.name() + "(" + bufferName + ")";
	}

	private String defineEncode(final String data, final Type type, final String bufferName, final CodeClass cls)
	{
		if (type instanceof Type.Primitive) {
			return bufferName + '.' + defineBufferMethod(type, false, false) + '(' + data + ')';
		}

		defineType(type, cls);

		if (type instanceof Type.Entity) {
			final Entity entity = ((Type.Entity)type).entity();

			if (entity.isEnum()) {
				return bufferName + ".writeEnum(" + data + ')';
			}

			encodes.add(entity);
			if (!entity.isEnum()) {
				if (entity.hasParent()) {
					Entity parent = entity.getParent();
					while (parent != null) {
						encodes.add(parent);
						parent = parent.getParent();
					}
				}
				Collections.addAll(encodes, entity.getChildren());
			}

			return bufferName + ".writeEntity(" + data + ')';
		}

		if (type instanceof Type.List) {
			final Type type1 = ((Type.List)type).type;
			if (type1 instanceof Type.Primitive) {
				return bufferName + '.' + defineBufferMethod(type1, false, true) + '(' + data + ')';
			}
			if (type1 instanceof Type.Entity) {
				if (((Type.Entity)type1).entity().isEnum()) {
					return bufferName + ".writeEnumArray(" + data + ')';
				}
				return bufferName + ".writeEntityArray(" + data + ')';
			}
		}

		cls.addImport(pkg + '.' + collectionFactory);

		encodeCollections.add(type);
		return collectionFactory + ".encode" + type.name() + "(" + bufferName + ", " + data + ')';
	}
}
