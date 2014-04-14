package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.Side;
import org.shypl.biser.compiler.Utils;
import org.shypl.biser.compiler.prototype.Entity;
import org.shypl.biser.compiler.prototype.EntityContainer;
import org.shypl.biser.compiler.prototype.Parameter;
import org.shypl.biser.compiler.prototype.Service;
import org.shypl.biser.compiler.prototype.ServiceMethod;
import org.shypl.biser.compiler.prototype.ServiceMethodResult;
import org.shypl.biser.compiler.prototype.Type;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BuilderFlash extends Builder
{
	public BuilderFlash(final Path path, final String stage, final String pkg, final Side side)
	{
		super(path, stage, pkg, side, "as", "CollectionFactory");
	}

	@Override
	protected void buildApiClient(final Service[] services) throws IOException
	{
		for (Service service : services) {
			if (service.hasClientMethods()) {
				buildApiClientServiceClient(service);
			}
			if (service.hasServerMethods()) {
				buildApiClientServiceServer(service);
			}
		}

		buildApiClientController(services);

		for (Service service : services) {
			for (Entity entity : service.getEntities()) {
				buildEntity(entity);
			}
		}
	}

	@Override
	protected void buildApiServer(final Service[] services)
	{
		throw new RuntimeException("Not supported");
	}

	@Override
	protected void buildCode(final CodeClass cls, final Code code)
	{
		// head
		code.line("package ", cls.pkg, "")
			.line("{");

		final String[] imports = cls.getImports();
		if (imports.length > 0) {
			for (String value : imports) {
				code.lineTab(1, "import ", value, ";");
			}
			code.line();
		}

		// declare
		code.addTab(1, cls.getModString(), cls.isInterface() ? "interface" : "class", " ", cls.name);
		if (cls.hasParent()) {
			code.add(" extends ", cls.getParent());
		}
		code.line()
			.lineTab(1, "{");

		// enum
		if (cls.isEnum()) {
			int i = 0;
			for (String value : cls.getEnumValues()) {
				code.lineTab(2, "public static const ", value, ":", cls.name,
					" = new ", cls.name, "(\"", value, "\", ", i++, ");");
			}
			code.line()
				.lineTab(2, "public function ", cls.name, "(name:String, ordinal:int)")
				.lineTab(2, "{")
				.lineTab(3, "super(name, ordinal);")
				.lineTab(2, "}")
				.line();
		}

		// properties
		final CodeParameter[] properties = cls.getProperties();
		if (properties.length > 0) {
			for (CodeParameter property : properties) {
				code.lineTab(2, property.getModString(), "var ", property.name, ":", property.type, ";");
			}
			code.line();
		}

		// constructors
		if (cls.hasConstructors()) {
			final CodeMethod constructor = cls.getConstructor();
			for (String line : constructor.head) {
				code.lineTab(2, line);
			}
			code.addTab(2, constructor.getModString(), "function ", constructor.name, "(");
			final CodeParameter[] arguments = constructor.getArguments();
			for (int i = 0; i < arguments.length; i++) {
				final CodeParameter argument = arguments[i];
				code.add(argument.name, ":", argument.type);
				if (argument.defaultValue != null) {
					code.add(" = ", argument.defaultValue);
				}
				if (i != arguments.length - 1) {
					code.add(", ");
				}
			}
			code.line("):void")
				.lineTab(2, "{")
				.add(constructor.body.toString(3))
				.lineTab(2, "}")
				.line();
		}

		// methods
		for (CodeMethod method : cls.getMethods()) {

			for (String line : method.head) {
				code.lineTab(2, line);
			}

			String mod = method.getModString();
			if (method.isOverride()) {
				mod = "override " + mod;
			}
			code.addTab(2, mod, "function ", method.isGetter() ? "get " : "", method.name, "(");

			final CodeParameter[] arguments = method.getArguments();
			for (int i = 0; i < arguments.length; i++) {
				final CodeParameter argument = arguments[i];
				code.add(argument.name, ":", argument.type);
				if (i != arguments.length - 1) {
					code.add(", ");
				}
			}
			code.add("):", method.result);

			if (cls.isInterface()) {
				code.line(";")
					.line();
			}
			else {
				code.line()
					.lineTab(2, "{")
					.add(method.body.toString(3))
					.lineTab(2, "}")
					.line();
			}
		}

		code.lineTab(1, "}")
			.line("}");
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

				method.addArgument("buffer", "InputBuffer");

				body.line("const l:int = buffer.readInt();")
					.line("if (l === -1) {")
					.lineTab(1, "return null;")
					.line("}");

				if (type instanceof Type.List) {
					final Type.List type1 = (Type.List)type;
					if (type1.isArray) {
						body.line("const r:", typeName, " = new ", typeName, "(l, true);")
							.line("for (var i:int = 0; i != l; ++i) {")
							.lineTab(1, "r[i] = ", defineDecode(type1.type, "buffer", cls), ";");
					}
					else {
						body.line("const r:", typeName, " = new ", typeName, "();")
							.line("for (var i:int = 0; i != l; ++i) {")
							.lineTab(1, "r.add(", defineDecode(type1.type, "buffer", cls), ");");
					}
				}
				else {
					final Type.Map type1 = (Type.Map)type;
					body.line("const r:", typeName, " = new ", typeName, "();")
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

				body.line("if (collection === null) {")
					.lineTab(1, "buffer.writeInt(-1);")
					.line("}");

				if (type instanceof Type.List) {
					final Type.List type1 = (Type.List)type;

					if (type1.isArray) {
						body.line("else if (collection.length === 0) {")
							.lineTab(1, "buffer.writeInt(0);")
							.line("}")
							.line("else {")
							.lineTab(1, "for each (var e:", defineType(type1.type, cls, true), " in collection) {")
							.lineTab(2, defineEncode("e", type1.type, "buffer", cls), ";")
							.lineTab(1, "}")
							.line("}");
					}
					else {
						body.line("else if (collection.empty) {")
							.lineTab(1, "buffer.writeInt(0);")
							.line("}")
							.line("else {")
							.lineTab(1, "const i:IListIterator = collection.iterator();")
							.lineTab(1, "while (i.next()) {")
							.lineTab(2, defineEncode("i.item", type1.type, "buffer", cls), ";")
							.lineTab(1, "}")
							.line("}");
					}
				}
				else {
					final Type.Map type1 = (Type.Map)type;

					body.line("else if (collection.empty) {")
						.lineTab(1, "buffer.writeInt(0);")
						.line("}")
						.line("else {")
						.lineTab(1, "const i:IMapIterator = collection.iterator();")
						.lineTab(1, "while (i.next()) {")
						.lineTab(2, defineEncode("i.key", type1.key, "buffer", cls), ";")
						.lineTab(2, defineEncode("i.value", type1.value, "buffer", cls), ";")
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
		if (decodeFactories.contains(entity)) {
			cls.addImport("org.shypl.biser.InputBuffer");
			cls.addImport("org.shypl.common.lang.IllegalArgumentException");

			final CodeMethod method = cls.addMethod("decodeFactory", Mod.STATIC | Mod.PUBLIC, cls.name);
			method.addArgument("buffer", "InputBuffer");

			method.body.line("var object:", cls.name, ";")
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
					constructor.addArgument(property.name, defineType(property.type, cls))
						.defaultValue = defineDefaultValue(property.type);
					names.add(property.name);
				}
				constructor.body.line("super(", Utils.join(names, ", "), ");");
			}

			if (entity.hasProperties()) {
				if (constructor == null) {
					constructor = cls.addConstructor();
				}
				for (Parameter property : properties) {
					constructor.addArgument(property.name, defineType(property.type, cls))
						.defaultValue = defineDefaultValue(property.type);
					constructor.body.line("this.", property.name, " = ", property.name, ";");
				}
			}
		}

		// decode
		if (decodes.contains(entity)) {
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
		if (encodes.contains(entity)) {
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

		// present
		if (entity.hasProperties()) {
			final CodeMethod method = cls.addMethod("represent", Mod.PROTECTED | Mod.OVERRIDE, VOID);
			cls.addImport("org.shypl.biser.Represent");
			method.addArgument("r", "Represent");

			if (hasParent) {
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
			cls.addImport("org.shypl.common.lang.IllegalArgumentException");

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
		if (entity.hasOwner()) {
			String name = entity.name;
			EntityContainer owner = entity.getOwner();
			while (true) {
				name = (owner instanceof Service ? owner.getFullName() : owner.name) + '_' + name;
				if (owner.hasOwner()) {
					owner = owner.getOwner();
				}
				else {
					break;
				}
			}
			return name;
		}

		return entity.name;
	}

	@Override
	protected void defineEntityDependencies(final CodeClass cls, final Entity entity)
	{
		super.defineEntityDependencies(cls, entity);

		if (entity.isEnum()) {
			cls.addImport("org.shypl.common.lang.OrdinalEnum");
			cls.setParent("OrdinalEnum");
		}
		else if (!entity.hasParent()) {
			cls.addImport("org.shypl.biser.BiserEntity");
			cls.setParent("BiserEntity");
		}
	}

	@Override
	protected String defineType(final Type type, final CodeClass cls, final boolean forCollection)
	{
		if (type instanceof Type.Primitive) {
			if (type == Type.Primitive.BOOL) {
				return "Boolean";
			}
			if (type == Type.Primitive.BYTE) {
				return "int";
			}
			if (type == Type.Primitive.BYTES) {
				return "ByteArray";
			}
			if (type == Type.Primitive.INT) {
				return "int";
			}
			if (type == Type.Primitive.UINT) {
				return "uint";
			}
			if (type == Type.Primitive.NUM) {
				return "Number";
			}
			if (type == Type.Primitive.DOUBLE) {
				return "Number";
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
				return "Vector.<" + defineType(type1.type, cls) + ">";
			}

			cls.addImport("org.shypl.common.collection.LinkedList");
			defineType(type1.type, cls);
			return "LinkedList";
		}

		if (type instanceof Type.Map) {
			Type.Map type1 = (Type.Map)type;
			cls.addImport("org.shypl.common.collection.LinkedMap");
			defineType(type1.key, cls);
			defineType(type1.value, cls);
			return "LinkedMap";
		}

		throw new IllegalArgumentException();
	}

	private String defineDefaultValue(final Type type)
	{
		if (type instanceof Type.Primitive) {
			if (type == Type.Primitive.BOOL) {
				return "false";
			}
			if (type == Type.Primitive.BYTE) {
				return "0";
			}
			if (type == Type.Primitive.INT) {
				return "0";
			}
			if (type == Type.Primitive.UINT) {
				return "0";
			}
			if (type == Type.Primitive.NUM) {
				return "0";
			}
			if (type == Type.Primitive.DOUBLE) {
				return "0";
			}
		}

		return "null";
	}

	private void buildApiClientController(final Service[] services) throws IOException
	{
		final CodeClass cls = createClass("Api", Mod.PUBLIC | Mod.FINAL, true);

		cls.addImport("org.shypl.biser.InputBuffer");
		cls.addImport("org.shypl.biser.client.AbstractApi");
		cls.addImport("org.shypl.biser.client.Channel");
		cls.addImport("org.shypl.common.lang.IllegalArgumentException");
		cls.setParent("AbstractApi");

		final CodeMethod constructor = cls.addConstructor();
		constructor.addArgument("_channel", "Channel");
		constructor.body.line("super(_channel);");

		final CodeMethod route = cls.addMethod("_route", Mod.OVERRIDE | Mod.PROTECTED, VOID);
		route.addArgument("service", "int");
		route.addArgument("method", "int");
		route.addArgument("buffer", "InputBuffer");
		route.body.line("switch (service) {");

		for (Service service : services) {
			if (service.hasClientMethods()) {
				final CodeParameter property = cls.addProperty("_c" + service.getFullName(), service.getFullName() + "Client", Mod.PRIVATE);

				constructor.addArgument(service.name, property.type);
				constructor.body.line(property.name, " = ", service.name, ";");

				route.body.lineTab(1, "case ", service.id, ":")
					.lineTab(2, "switch (method) {");

				for (ServiceMethod method : service.getClientMethods()) {

					route.body.lineTab(3, "case ", method.id, ":");

					if (method.hasArguments()) {
						final CodeMethod call = cls.addMethod("call_" + service.name + "_" + method.name, Mod.PRIVATE, VOID);

						route.body.addTab(4, "return ", call.name, "(");

						boolean s = false;
						String log = "";
						String args = "";
						for (Parameter arg : method.getArguments()) {
							if (s) {
								route.body.add(", ");
								args += ", ";
								log += ", ";
							}
							else {
								s = true;
							}
							args += arg.name;
							log += arg.name + ": {}";
							route.body.add(defineDecode(arg.type, "buffer", cls));
							call.addArgument(arg.name, defineType(arg.type, cls));
						}

						route.body.line(");");

						call.body.line("if (_logger.debugEnabled) {")
							.lineTab(1, "_logger.debug(\"< ", service.name, ".", method.name, "(", log, ")\", ", args, ");")
							.line("}")
							.line(property.name, ".", method.name, "(", args, ");");
					}
					else {
						route.body.lineTab(4, "if (_logger.debugEnabled) {")
							.lineTab(5, "_logger.debug(\"< ", service.name, ".", method.name, "\");")
							.lineTab(4, "}")
							.lineTab(4, "return ", property.name, ".", method.name, "();");
					}
				}

				route.body.lineTab(2, "}");
			}

			if (service.hasServerMethods()) {
				final CodeParameter property = cls.addProperty("_s" + service.getFullName(), service.getFullName() + "Server", Mod.PRIVATE);

				constructor.body.line(property.name, " = new ", property.type, "(this);");

				cls.addMethod(service.name, Mod.PUBLIC | Mod.GETTER, property.type)
					.body.line("return ", property.name, ";");
			}
		}

		route.body.line("}")
			.line("throw new IllegalArgumentException(\"service: \" + service + \", method: \" + method);");

		save(cls);
	}

	private void buildApiClientServiceClient(final Service service) throws IOException
	{
		final CodeClass cls = createClass(service.getFullName() + "Client", Mod.PUBLIC | Mod.INTERFACE, true);

		for (ServiceMethod serviceMethod : service.getClientMethods()) {
			final CodeMethod method = cls.addMethod(serviceMethod.name, 0, VOID);
			for (Parameter argument : serviceMethod.getArguments()) {
				method.addArgument(argument.name, defineType(argument.type, cls));
			}
		}

		save(cls);
	}

	private void buildApiClientServiceServer(final Service service) throws IOException
	{
		final CodeClass cls = createClass(service.getFullName() + "Server", Mod.PUBLIC | Mod.FINAL, true);

		cls.addImport("org.shypl.biser.OutputBuffer");
		cls.addImport("org.shypl.biser.client.Service");
		cls.setParent("Service");

		final CodeMethod constructor = cls.addConstructor();
		constructor.addArgument("api", "Api");
		constructor.body.line("super(", service.id, ", api);");

		// methods
		for (ServiceMethod serviceMethod : service.getServerMethods()) {
			final CodeMethod method = cls.addMethod(serviceMethod.name, Mod.PUBLIC | Mod.FINAL, VOID);
			final Code body = method.body;

			boolean hasArguments = false;
			String log1 = "";
			String log2 = "";
			for (Parameter arg : serviceMethod.getArguments()) {
				method.addArgument(arg.name, defineType(arg.type, cls));
				if (hasArguments) {
					log1 += ", ";
					log2 += ", ";
				}
				else {
					hasArguments = true;
				}
				log1 += arg.name + ": {}";
				log2 += arg.name;
			}

			// debug
			body.line("if (_logger.debugEnabled) {");
			if (hasArguments) {
				body.lineTab(1, "_logger.debug(\"> ", service.name, ".", method.name, "(", log1, ")\", ", log2, ");");
			}
			else {
				body.lineTab(1, "_logger.debug(\"> ", service.name, ".", method.name, "\");");
			}
			body.line("}");

			//
			if (serviceMethod.hasResult()) {
				final ResultWrap result = new ResultWrap(service, serviceMethod);
				buildApiClientServiceServerResultHandler(result);
				buildApiClientServiceServerResultHandlerHolder(result);
				method.addArgument("_handler", result.handlerName);
				if (hasArguments) {
					body.line("const _m:OutputBuffer = _createMessageWithResult(", serviceMethod.id, ", new ", result.holderName, "(_handler));");
				}
				else {
					body.line("_sendMessage(_createMessageWithResult(", serviceMethod.id, ", new ", result.holderName, "(_handler)));");
				}
			}
			else {
				if (hasArguments) {
					body.line("const _m:OutputBuffer = _createMessage(", serviceMethod.id, ");");
				}
				else {
					body.line("_sendMessage(_createMessage(", serviceMethod.id, "));");
				}
			}

			if (hasArguments) {
				for (Parameter arg : serviceMethod.getArguments()) {
					body.line(defineEncode(arg.name, arg.type, "_m", cls), ";");
				}
				body.line("_sendMessage(_m);");
			}
		}

		save(cls);
	}

	private void buildApiClientServiceServerResultHandler(final ResultWrap result) throws IOException
	{
		final CodeClass cls = createClass(result.handlerName, Mod.PUBLIC | Mod.INTERFACE, true);
		final CodeMethod method = cls.addMethod(result.methodName, 0, VOID);
		final ServiceMethodResult methodResult = result.method.getResult();

		if (methodResult instanceof ServiceMethodResult.TypeRef) {
			method.addArgument("result", defineType(((ServiceMethodResult.TypeRef)methodResult).type, cls));
		}
		else {
			final ServiceMethodResult.LazyMulti multi = (ServiceMethodResult.LazyMulti)methodResult;
			for (Parameter param : multi.getParameters()) {
				method.addArgument(param.name, defineType(param.type, cls));
			}
		}

		save(cls);
	}

	private void buildApiClientServiceServerResultHandlerHolder(final ResultWrap result) throws IOException
	{
		final CodeClass cls = createClass(result.holderName, Mod.INTERNAL | Mod.FINAL, true);

		cls.addImport("org.shypl.biser.InputBuffer");
		cls.addImport("org.shypl.biser.client.ResultHandlerHolder");
		cls.setParent("ResultHandlerHolder");

		cls.addProperty("_handler", result.handlerName, Mod.PRIVATE);

		CodeMethod method = cls.addConstructor();
		method.addArgument("handler", result.handlerName);
		method.body.line("_handler = handler;");

		method = cls.addMethod("handle", Mod.OVERRIDE | Mod.PROTECTED, VOID);
		method.addArgument("_m", "InputBuffer");

		final ServiceMethodResult methodResult = result.method.getResult();
		if (methodResult instanceof ServiceMethodResult.TypeRef) {
			final Type type = ((ServiceMethodResult.TypeRef)methodResult).type;
			method.body.line("const result:", defineType(type, cls), " = ", defineDecode(type, "_m", cls), ";")
				.line("if (_logger.debugEnabled) {")
				.line("_logger.debug(\"<< ", result.serviceName, ".", result.methodName, ": {}\", result);")
				.line("}")
				.line("_handler.", result.methodName, "(result);");
		}
		else {
			final CodeMethod call = cls.addMethod("_call", Mod.PRIVATE, VOID);
			final ServiceMethodResult.LazyMulti multi = (ServiceMethodResult.LazyMulti)methodResult;

			String log1 = "";
			String args = "";
			boolean hasArguments = false;
			method.body.add("_call(");
			for (Parameter param : multi.getParameters()) {
				call.addArgument(param.name, defineType(param.type, cls));
				if (hasArguments) {
					method.body.add(", ");
					log1 += ", ";
					args += ", ";
				}
				else {
					hasArguments = true;
				}
				method.body.add(defineDecode(param.type, "_m", cls));
				log1 += param.name + ": {}";
				args += param.name;
			}
			method.body.line(");");

			call.body.line("if (_logger.debugEnabled) {")
				.lineTab(1, "_logger.debug(\"<< ", result.serviceName, ".", result.methodName, ":(", log1, ")\", ", args, ");")
				.line("}")
				.line("_handler.", result.methodName, "(", args, ");");
		}

		method.body.line("_handler = null;");

		save(cls);
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

		if (type instanceof Type.Entity) {
			final Entity entity = ((Type.Entity)type).entity();
			decodeFactories.add(entity);

			if (entity.isEnum()) {
				return defineEntityClassName(entity) + ".decode(" + bufferName + ")";
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
			return defineEntityClassName(entity) + ".decodeFactory(" + bufferName + ")";
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

	///

	private static class ResultWrap
	{
		public final String        serviceName;
		public final String        methodName;
		public final String        handlerName;
		public final String        holderName;
		public final ServiceMethod method;

		public ResultWrap(final Service service, final ServiceMethod method)
		{
			this.method = method;
			final String name = Utils.toCamelCase(service.name + "_" + method.name);
			serviceName = service.name;
			methodName = "handle" + name;
			handlerName = name + "Handler";
			holderName = handlerName + "Holder";
		}
	}
}
