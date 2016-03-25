package org.shypl.biser.compiler.builder.java;

import org.shypl.biser.compiler.Utils;
import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeEngine;
import org.shypl.biser.compiler.code.CodeExpression;
import org.shypl.biser.compiler.code.CodeExpressionLambda;
import org.shypl.biser.compiler.code.CodeExpressionMethod;
import org.shypl.biser.compiler.code.CodeExpressionNew;
import org.shypl.biser.compiler.code.CodeExpressionString;
import org.shypl.biser.compiler.code.CodeExpressionStringConcat;
import org.shypl.biser.compiler.code.CodeExpressionVar;
import org.shypl.biser.compiler.code.CodeExpressionWord;
import org.shypl.biser.compiler.code.CodeGeneric;
import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.CodeModifier;
import org.shypl.biser.compiler.code.CodeParameter;
import org.shypl.biser.compiler.code.CodeParametrizedClass;
import org.shypl.biser.compiler.code.CodePrimitive;
import org.shypl.biser.compiler.code.CodeStatementBlock;
import org.shypl.biser.compiler.code.CodeStatementBreak;
import org.shypl.biser.compiler.code.CodeStatementReturn;
import org.shypl.biser.compiler.code.CodeStatementSwitch;
import org.shypl.biser.compiler.code.CodeStatementSwitchCase;
import org.shypl.biser.compiler.code.CodeStatementThrow;
import org.shypl.biser.compiler.code.CodeType;
import org.shypl.biser.compiler.model.Api;
import org.shypl.biser.compiler.model.ApiService;
import org.shypl.biser.compiler.model.ApiServiceMethod;
import org.shypl.biser.compiler.model.ArrayType;
import org.shypl.biser.compiler.model.DataType;
import org.shypl.biser.compiler.model.EntityType;
import org.shypl.biser.compiler.model.EnumType;
import org.shypl.biser.compiler.model.MapType;
import org.shypl.biser.compiler.model.Parameter;
import org.shypl.biser.compiler.model.PrimitiveType;
import org.shypl.biser.compiler.model.TypeRepresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class JavaCodeBuilder extends OopCodeBuilder {

	private final Map<PrimitiveType, CodeType> primitives           = new HashMap<>();
	private final Map<PrimitiveType, CodeType> primitiveObjects     = new HashMap<>();
	private final Map<PrimitiveType, String>   constPrimitives      = new HashMap<>();
	private final Map<PrimitiveType, String>   constPrimitiveArrays = new HashMap<>();
	private final Map<PrimitiveType, String>   primitiveCamelNames  = new HashMap<>();

	private final TypeEncoderDefender typeEncoderDefender;
	private final TypeDecoderDefender typeDecoderDefender;

	private final CodePrimitive primitiveVoid;
	private final CodePrimitive primitiveInt;
	private final CodeClass     primitiveString;

	public JavaCodeBuilder(String pack) {
		super(new CodeEngine('.'), pack);

		typeDecoderDefender = new TypeDecoderDefender();
		typeEncoderDefender = new TypeEncoderDefender();

		primitiveVoid = engine.getPrimitive("void");
		primitiveInt = engine.getPrimitive("int");
		primitiveString = engine.getClass("java.lang.String");

		primitives.put(PrimitiveType.BYTE, engine.getPrimitive("byte"));
		primitives.put(PrimitiveType.BOOL, engine.getPrimitive("boolean"));
		primitives.put(PrimitiveType.INT, primitiveInt);
		primitives.put(PrimitiveType.UINT, engine.getPrimitive("long"));
		primitives.put(PrimitiveType.LONG, engine.getPrimitive("long"));
		primitives.put(PrimitiveType.ULONG, engine.getPrimitive("long"));
		primitives.put(PrimitiveType.DOUBLE, engine.getPrimitive("double"));
		primitives.put(PrimitiveType.STRING, primitiveString);
		primitives.put(PrimitiveType.DATE, engine.getClass("java.util.Date"));
		primitives.put(PrimitiveType.BYTES, engine.getArray(engine.getPrimitive("byte")));

		primitiveObjects.put(PrimitiveType.BYTE, engine.getClass("java.lang.Byte"));
		primitiveObjects.put(PrimitiveType.BOOL, engine.getClass("java.lang.Boolean"));
		primitiveObjects.put(PrimitiveType.INT, engine.getClass("java.lang.Integer"));
		primitiveObjects.put(PrimitiveType.UINT, engine.getClass("java.lang.Long"));
		primitiveObjects.put(PrimitiveType.LONG, engine.getClass("java.lang.Long"));
		primitiveObjects.put(PrimitiveType.ULONG, engine.getClass("java.lang.Long"));
		primitiveObjects.put(PrimitiveType.DOUBLE, engine.getClass("java.lang.Double"));
		primitiveObjects.put(PrimitiveType.STRING, primitiveString);
		primitiveObjects.put(PrimitiveType.DATE, primitives.get(PrimitiveType.DATE));
		primitiveObjects.put(PrimitiveType.BYTES, primitives.get(PrimitiveType.BYTES));

		constPrimitives.put(PrimitiveType.BYTE, "BYTE");
		constPrimitives.put(PrimitiveType.BOOL, "BOOL");
		constPrimitives.put(PrimitiveType.INT, "INT");
		constPrimitives.put(PrimitiveType.UINT, "UINT");
		constPrimitives.put(PrimitiveType.LONG, "LONG");
		constPrimitives.put(PrimitiveType.ULONG, "ULONG");
		constPrimitives.put(PrimitiveType.DOUBLE, "DOUBLE");
		constPrimitives.put(PrimitiveType.STRING, "STRING");
		constPrimitives.put(PrimitiveType.DATE, "DATE");
		constPrimitives.put(PrimitiveType.BYTES, "BYTES");

		constPrimitiveArrays.put(PrimitiveType.BYTE, "BYTE_ARRAY");
		constPrimitiveArrays.put(PrimitiveType.BOOL, "BOOL_ARRAY");
		constPrimitiveArrays.put(PrimitiveType.INT, "INT_ARRAY");
		constPrimitiveArrays.put(PrimitiveType.UINT, "UINT_ARRAY");
		constPrimitiveArrays.put(PrimitiveType.LONG, "LONG_ARRAY");
		constPrimitiveArrays.put(PrimitiveType.ULONG, "ULONG_ARRAY");
		constPrimitiveArrays.put(PrimitiveType.DOUBLE, "DOUBLE_ARRAY");

		primitiveCamelNames.put(PrimitiveType.BYTE, "Byte");
		primitiveCamelNames.put(PrimitiveType.BOOL, "Bool");
		primitiveCamelNames.put(PrimitiveType.INT, "Int");
		primitiveCamelNames.put(PrimitiveType.UINT, "Uint");
		primitiveCamelNames.put(PrimitiveType.LONG, "Long");
		primitiveCamelNames.put(PrimitiveType.ULONG, "Ulong");
		primitiveCamelNames.put(PrimitiveType.DOUBLE, "Double");
		primitiveCamelNames.put(PrimitiveType.STRING, "String");
		primitiveCamelNames.put(PrimitiveType.DATE, "Date");
		primitiveCamelNames.put(PrimitiveType.BYTES, "Bytes");
	}

	@Override
	public CodeType representPrimitive(PrimitiveType type) {
		return primitives.get(type);
	}

	@Override
	public CodeType representMap(MapType type) {
		return engine.getClass("java.util.Map").parametrize(representAsObject(type.getKeyType()), representAsObject(type.getValueType()));
	}

	@Override
	public void buildEntity(EntityType type) {
		CodeClass cls = modulePackage.getClass(type.getName());
		cls.getModifier().set(CodeModifier.PUBLIC);

		if (type.hasParent()) {
			cls.setParent(cls.getPackage().getClass(type.getParent().getName()));
		}
		else {
			cls.setParent(engine.getClass("org.shypl.biser.io.Entity"));
		}

		// fields
		for (Parameter typeField : type.getFields()) {
			CodeParameter classField = cls.getField(typeField.getName());
			classField.getModifier().set(CodeModifier.PUBLIC);
			classField.setType(getType(typeField.getType()));
		}

		// empty constructor
		CodeStatementBlock methodBody;
		CodeMethod method = cls.addMethod(cls.getName());
		method.getModifier().set(CodeModifier.PUBLIC);

		// full constructor
		if (type.hasAllFields()) {
			method = cls.addMethod(cls.getName());
			method.getModifier().set(CodeModifier.PUBLIC);

			for (Parameter field : type.getAllFields()) {
				CodeParameter argument = method.getArgument(field.getName());
				argument.setType(getType(field.getType()));
			}
			methodBody = method.getBody();

			if (type.hasParent()) {
				CodeExpressionMethod superCall = new CodeExpressionMethod("super");
				methodBody.addStatement(superCall);
				for (Parameter field : type.getParent().getAllFields()) {
					superCall.addArgument(field.getName());
				}
			}

			for (Parameter field : type.getFields()) {
				methodBody.addStatement(CodeExpressionWord.THIS.field(field.getName()).assign(field.getName()));
			}
		}

		// biser decoder
		CodeParameter decoder = cls.getField("_DECODER");
		decoder.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.FINAL);
		CodeParametrizedClass entityDecoderClass = engine.getClass("org.shypl.biser.io.EntityDecoder").parametrize(cls);
		CodeExpressionNew expressionNew = new CodeExpressionNew(entityDecoderClass, cls.field("class"));
		decoder.setType(entityDecoderClass);
		decoder.setValue(expressionNew);
		method = expressionNew.addMethod("factory");
		method.getModifier().set(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
		method.setReturnType(cls);
		method.getArgument("id").setType(primitiveInt);

		CodeStatementSwitch swt = new CodeStatementSwitch(method.getArgument("id").getVariable());
		method.getBody().addStatement(swt);
		swt.getDefaultCase().addStatement(new CodeStatementReturn(CodeExpressionWord.NULL));
		swt.addCase(String.valueOf(type.getId())).addStatement(new CodeStatementReturn(new CodeExpressionNew(cls)));
		for (EntityType childType : type.getChildren()) {
			swt.addCase(String.valueOf(childType.getId())).addStatement(new CodeStatementReturn(new CodeExpressionNew(getType(childType))));
		}


		// biser id
		if (type.getId() != 0) {
			method = cls.addMethod("_id");
			method.getModifier().set(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
			method.setReturnType(primitiveInt);
			method.getBody().addStatement(new CodeStatementReturn(String.valueOf(type.getId())));
		}

		// biser encode decode
		if (type.hasFields()) {
			method = cls.addMethod("_encode");
			method.getModifier().set(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			method.getArgument("writer").setType(engine.getClass("org.shypl.biser.io.DataWriter"));
			CodeStatementBlock bodyEncode = method.getBody();

			method = cls.addMethod("_decode");
			method.getModifier().set(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			method.getArgument("reader").setType(engine.getClass("org.shypl.biser.io.DataReader"));
			CodeStatementBlock bodyDecode = method.getBody();

			CodeExpressionWord writer = new CodeExpressionWord("writer");
			CodeExpressionWord reader = new CodeExpressionWord("reader");
			if (type.hasParent()) {
				bodyEncode.addStatement(CodeExpressionWord.SUPER.method("_encode", writer));
				bodyDecode.addStatement(CodeExpressionWord.SUPER.method("_decode", reader));
			}

			for (Parameter field : type.getFields()) {
				CodeExpression classField = CodeExpressionWord.THIS.field(field.getName());
				bodyEncode.addStatement(defineEncode(classField, writer, field.getType()));
				bodyDecode.addStatement(defineDecode(classField, reader, field.getType()));
			}
		}

		// to string
		method = cls.addMethod("_toString");
		method.getModifier().set(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
		method.setReturnType(primitiveVoid);
		method.getArgument("fields").setType(engine.getClass("java.util.Map").parametrize(primitiveString, primitiveString));
		methodBody = method.getBody();
		CodeExpressionWord fields = new CodeExpressionWord("fields");

		if (type.hasParent()) {
			methodBody.addStatement(new CodeExpressionMethod("super._toString", fields));
		}

		CodeClass stringUtils = engine.getClass("org.shypl.common.util.StringUtils");

		for (Parameter field : type.getFields()) {
			methodBody.addStatement(fields.method("put",
				new CodeExpressionString(field.getName()), stringUtils.method("toString", CodeExpressionWord.THIS.field(field.getName()))));
		}
	}

	@Override
	public void buildEnum(EnumType type) {
		CodeClass cls = modulePackage.getClass(type.getName());
		cls.getModifier().set(CodeModifier.PUBLIC);
		cls.addEnumValues(type.getValues());

		CodeParameter field = cls.getField("_DECODER");
		field.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.FINAL);
		field.setType(engine.getClass("org.shypl.biser.io.EnumDecoder").parametrize(cls));
		field.setValue(new CodeExpressionNew(engine.getClass("org.shypl.biser.io.EnumDecoder").parametrize(), cls.field("class")));
	}

	@Override
	public void buildServerApi(Api api) {
		ServerApiBuilder builder = new ServerApiBuilder(api.getName());
		builder.buildClient(api.getClientServices());
		builder.buildServer(api.getServerServices());
	}

	@Override
	public void buildClientApi(Api api) {
		throw new IllegalStateException("Not supported");
	}

	private CodeType representAsObject(DataType type) {
		if (type instanceof PrimitiveType) {
			return primitiveObjects.get(type);
		}
		return type.represent(this);
	}

	private CodeExpression defineEncode(CodeExpression target, CodeExpression writer, DataType type) {
		return typeEncoderDefender.define(target, writer, type);
	}

	private CodeExpression defineDecode(CodeExpression target, CodeExpression reader, DataType type) {
		return typeDecoderDefender.define(target, reader, type);
	}

	private class TypeEncoderDefender implements TypeRepresenter<CodeExpression> {

		private final TypeEncoder    encoder;
		private       CodeExpression target;
		private       CodeExpression writer;

		public TypeEncoderDefender() {
			encoder = new TypeEncoder();
		}

		public CodeExpression define(CodeExpression target, CodeExpression writer, DataType type) {
			this.target = target;
			this.writer = writer;
			return type.represent(this);
		}

		@Override
		public CodeExpression representPrimitive(PrimitiveType type) {
			return writer.method("write" + primitiveCamelNames.get(type), target);
		}

		@Override
		public CodeExpression representEntity(EntityType type) {
			return writer.method("writeEntity", target);
		}

		@Override
		public CodeExpression representEnum(EnumType type) {
			return writer.method("writeEnum", target);
		}

		@Override
		public CodeExpression representArray(ArrayType type) {
			DataType elementType = type.getElementType();
			if (elementType instanceof PrimitiveType) {
				if (constPrimitiveArrays.containsKey(elementType)) {
					return writer.method("write" + primitiveCamelNames.get(elementType) + "Array", target);
				}
			}

			return writer.method("writeArray", target, elementType.represent(encoder));
		}

		@Override
		public CodeExpression representMap(MapType type) {
			return writer.method("writeMap", target, type.getKeyType().represent(encoder), type.getValueType().represent(encoder));
		}

		private class TypeEncoder implements TypeRepresenter<CodeExpression> {
			private final CodeClass encoder;

			public TypeEncoder() {
				encoder = engine.getClass("org.shypl.biser.io.Encoder");
			}

			@Override
			public CodeExpression representPrimitive(PrimitiveType type) {
				return encoder.field(constPrimitives.get(type));
			}

			@Override
			public CodeExpression representEntity(EntityType type) {
				return encoder.field("ENTITY");
			}

			@Override
			public CodeExpression representEnum(EnumType type) {
				return encoder.field("ENUM");
			}

			@Override
			public CodeExpression representArray(ArrayType type) {
				DataType elementType = type.getElementType();

				if (elementType instanceof PrimitiveType) {
					if (constPrimitiveArrays.containsKey(elementType)) {
						return encoder.field(constPrimitiveArrays.get(elementType));
					}
				}

				return encoder.method("forArray", elementType.represent(this));
			}

			@Override
			public CodeExpression representMap(MapType type) {
				return encoder.method("forMap", type.getKeyType().represent(this), type.getValueType().represent(this));
			}
		}
	}

	private class TypeDecoderDefender implements TypeRepresenter<CodeExpression> {

		private final TypeDecoder    decoder;
		private       CodeExpression reader;

		public TypeDecoderDefender() {
			decoder = new TypeDecoder();
		}

		public CodeExpression define(CodeExpression target, CodeExpression reader, DataType type) {
			this.reader = reader;
			return target.assign(type.represent(this));
		}

		@Override
		public CodeExpression representPrimitive(PrimitiveType type) {
			return reader.method("read" + primitiveCamelNames.get(type));
		}

		@Override
		public CodeExpression representEntity(EntityType type) {
			return decode(type);
		}

		@Override
		public CodeExpression representEnum(EnumType type) {
			return decode(type);
		}

		@Override
		public CodeExpression representArray(ArrayType type) {
			DataType elementType = type.getElementType();
			if (elementType instanceof PrimitiveType) {
				if (constPrimitiveArrays.containsKey(elementType)) {
					return reader.method("read" + primitiveCamelNames.get(elementType) + "Array");
				}
			}

			return reader.method("readArray", elementType.represent(decoder));
		}

		@Override
		public CodeExpression representMap(MapType type) {
			return reader.method("readMap", type.getKeyType().represent(decoder), type.getValueType().represent(decoder));
		}

		private CodeExpression decode(DataType type) {
			return type.represent(decoder).method("decode", reader);
		}

		private class TypeDecoder implements TypeRepresenter<CodeExpression> {
			private final CodeClass decoder;

			public TypeDecoder() {
				decoder = engine.getClass("org.shypl.biser.io.Decoder");
			}

			@Override
			public CodeExpression representPrimitive(PrimitiveType type) {
				return decoder.field(constPrimitives.get(type));
			}

			@Override
			public CodeExpression representEntity(EntityType type) {
				return getType(type).field("_DECODER");
			}

			@Override
			public CodeExpression representEnum(EnumType type) {
				return getType(type).field("_DECODER");
			}

			@Override
			public CodeExpression representArray(ArrayType type) {
				DataType elementType = type.getElementType();

				if (elementType instanceof PrimitiveType) {
					if (constPrimitiveArrays.containsKey(elementType)) {
						return decoder.field(constPrimitiveArrays.get(elementType));
					}
				}

				return decoder.method("forArray", elementType.represent(this));
			}

			@Override
			public CodeExpression representMap(MapType type) {
				return decoder.method("forMap", type.getKeyType().represent(this), type.getValueType().represent(this));
			}
		}
	}

	private class ServerApiBuilder extends SubPackageBuilder {

		private final CodeClass clientClass;

		public ServerApiBuilder(String apiName) {
			super(apiName);
			clientClass = pack.getClass(Utils.convertToCamel(name) + "Client");
		}

		public void buildClient(List<ApiService> services) {
			CodeClass cls = clientClass;
			cls.getModifier().set(CodeModifier.PUBLIC);
			cls.setParent(engine.getClass("org.shypl.biser.csi.server.Client"));

			// constructor
			CodeMethod method = cls.addMethod(cls.getName());
			method.getModifier().set(CodeModifier.PUBLIC);
			method.getArgument("id").setType(engine.getPrimitive("long"));

			CodeStatementBlock body = method.getBody();
			body.addStatement(new CodeExpressionMethod("super", new CodeExpressionWord("id")));

			if (!services.isEmpty()) {
				buildClientServices(services);
				CodeParameter api = cls.getField("api");
				api.setType(pack.getClass("ClientServices"));
				api.getModifier().add(CodeModifier.PUBLIC | CodeModifier.FINAL);
				body.addStatement(new CodeExpressionWord("api").assign(new CodeExpressionNew(pack.getClass("ClientServices"), CodeExpressionWord.THIS)));
			}
		}

		public void buildServer(List<ApiService> services) {
			CodeClass cls = pack.getClass(Utils.convertToCamel(name) + "Api");
			CodeGeneric c = cls.getGeneric("C");
			c.setDependence(CodeGeneric.Dependence.EXTENDS, clientClass);
			cls.setParent(engine.getClass("org.shypl.biser.csi.server.Api").parametrize(c));
			cls.getModifier().add(CodeModifier.ABSTRACT | CodeModifier.PUBLIC);

			// constructor
			CodeMethod method = cls.addMethod(cls.getName());
			method.getModifier().set(CodeModifier.PUBLIC);
			method.getBody().addStatement(new CodeExpressionMethod("super", new CodeExpressionString(name)));

			// callService
			method = cls.addMethod("callService");
			method.getModifier().add(CodeModifier.PROTECTED | CodeModifier.FINAL | CodeModifier.OVERRIDE);
			method.addThrows(engine.getClass("java.lang.Throwable"));
			method.setReturnType(primitiveVoid);

			method.getArgument("client").setType(c);
			CodeExpression serviceId = method.getArgument("serviceId").setType(primitiveInt).getVariable();
			CodeExpression actionId = method.getArgument("actionId").setType(primitiveInt).getVariable();
			method.getArgument("reader").setType(engine.getClass("org.shypl.biser.io.DataReader"));
			method.getArgument("writer").setType(engine.getClass("org.shypl.biser.io.DataWriter"));


			CodeStatementSwitch serviceSwitch = new CodeStatementSwitch(serviceId);
			method.getBody()
				.addStatement(serviceSwitch)
				.addStatement(new CodeStatementThrow(new CodeExpressionNew(
					engine.getClass("org.shypl.biser.csi.ProtocolException"), new CodeExpressionStringConcat(
					new CodeExpressionString("Action not exists: " + pack.getName() + ".#"), serviceId, new CodeExpressionString(".#"), actionId
				))));

			for (ApiService service : services) {
				CodeStatementSwitchCase serviceCase = serviceSwitch.addCase(String.valueOf(service.getId()));
				CodeClass serviceClass = buildServerService(cls, service, serviceCase);

				CodeParameter field = cls.getField("service" + service.getCamelName());
				field.getModifier().add(CodeModifier.PRIVATE);
				field.setType(serviceClass.parametrize(c));

				method = cls.addMethod("register" + serviceClass.getName());
				method.getModifier().set(CodeModifier.PUBLIC | CodeModifier.FINAL);
				method.setReturnType(primitiveVoid);
				method.getArgument("service").setType(serviceClass.parametrize(c));

//				CodeStatementIf statementIf = new CodeStatementIf(
//					new CodeExpressionBinaryOperator("!=", CodeExpressionWord.THIS.field(field.getName()), CodeExpressionWord.NULL));
//				statementIf.addStatement(new CodeStatementThrow(new CodeExpressionNew(engine.getClass("java.lang.RuntimeException"))));

				method.getBody()
//					.addStatement(statementIf)
					.addStatement(CodeExpressionWord.THIS.field(field.getName()).assign(new CodeExpressionWord("service")));

				serviceCase.addStatement(CodeStatementBreak.INSTANCE);
			}
		}

		private void buildClientServices(List<ApiService> services) {
			CodeClass cls = pack.getClass("ClientServices");
			cls.getModifier().set(CodeModifier.FINAL | CodeModifier.PUBLIC);
			CodeMethod method = cls.addMethod(cls.getName());
			method.getArgument("client").setType(clientClass);
			CodeStatementBlock body = method.getBody();

			CodeExpressionWord client = new CodeExpressionWord("client");
			for (ApiService service : services) {
				CodeClass serviceClass = buildClientService(service);
				CodeParameter field = cls.getField(service.getName());
				field.setType(serviceClass);
				field.getModifier().add(CodeModifier.PUBLIC | CodeModifier.FINAL);
				body.addStatement(CodeExpressionWord.THIS.field(service.getName()).assign(new CodeExpressionNew(serviceClass, client)));
			}
		}

		private CodeClass buildClientService(ApiService service) {
			CodeClass cls = pack.getClass("ClientService" + service.getCamelName());
			cls.getModifier().add(CodeModifier.FINAL | CodeModifier.PUBLIC);
			cls.setParent(engine.getClass("org.shypl.biser.csi.server.ClientService"));

			CodeMethod method = cls.addMethod(cls.getName());
			method.getArgument("client").setType(clientClass);
			method.getBody().addStatement(new CodeExpressionMethod("super",
				new CodeExpressionWord(service.getId()),
				new CodeExpressionString(service.getName()),
				new CodeExpressionWord("client")));

			for (ApiServiceMethod action : service.getClientActions()) {
				if (action.hasResult()) {
					throw new IllegalStateException("Not supported");
				}
				method = cls.addMethod(action.getName());
				method.getModifier().add(CodeModifier.PUBLIC);
				method.setReturnType(primitiveVoid);
				CodeStatementBlock body = method.getBody();

				CodeExpressionMethod log = new CodeExpressionMethod("_log", new CodeExpressionString(action.getName()));
				body.addStatement(log);

				CodeExpressionWord writer = new CodeExpressionWord("_writer");
				CodeExpressionLambda lambda = new CodeExpressionLambda(writer.getWord());
				CodeStatementBlock lambdaBody = lambda.getBody();

				body.addStatement(new CodeExpressionMethod("_send", new CodeExpressionWord(action.getId()), lambda));

				for (Parameter arg : action.getArguments()) {
					method.getArgument(arg.getName()).setType(getType(arg.getType()));
					log.addArgument(arg.getName());
					lambdaBody.addStatement(defineEncode(new CodeExpressionWord(arg.getName()), writer, arg.getType()));
				}

				if (action.isGlobal()) {
					CodeClass messageClass = pack.getClass("Message" + service.getCamelName() + action.getCamelName());
					messageClass.getModifier().add(CodeModifier.PUBLIC | CodeModifier.FINAL);
					messageClass.setParent(engine.getClass("org.shypl.biser.csi.server.GlobalMessage"));

					method = messageClass.addMethod(messageClass.getName());
					method.getModifier().add(CodeModifier.PUBLIC);
					body = method.getBody();
					body.addStatement(new CodeExpressionMethod("super",
						new CodeExpressionWord(service.getId()),
						new CodeExpressionString(service.getName()),
						new CodeExpressionWord(action.getId()),
						new CodeExpressionString(action.getName())
					));

					for (Parameter arg : action.getArguments()) {
						method.getArgument(arg.getName()).setType(getType(arg.getType()));
						CodeParameter field = messageClass.getField(arg.getName());
						field.setType(getType(arg.getType()));
						field.getModifier().add(CodeModifier.PRIVATE | CodeModifier.FINAL);
						body.addStatement(CodeExpressionWord.THIS.field(arg.getName()).assign(new CodeExpressionWord(arg.getName())));
					}

					method = messageClass.addMethod("make");
					method.getModifier().add(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
					method.getArgument("_writer").setType(engine.getClass("org.shypl.biser.io.DataWriter"));
					method.setReturnType(primitiveVoid);
					body = method.getBody();

					log = new CodeExpressionMethod("log");
					body.addStatement(log);
					writer = new CodeExpressionWord("_writer");

					for (Parameter arg : action.getArguments()) {
						log.addArgument(arg.getName());
						body.addStatement(defineEncode(new CodeExpressionWord(arg.getName()), writer, arg.getType()));
					}
				}
			}

			return cls;
		}

		private CodeClass buildServerService(CodeClass gate, ApiService service, CodeStatementSwitchCase serviceCase) {
			CodeClass cls = pack.getClass("Service" + service.getCamelName());
			CodeGeneric c = cls.getGeneric("C");
			c.setDependence(CodeGeneric.Dependence.EXTENDS, clientClass);
			cls.setInterface(true);
			cls.getModifier().add(CodeModifier.PUBLIC);

			CodeExpressionWord reader = new CodeExpressionWord("reader");
			CodeExpressionWord writer = new CodeExpressionWord("writer");

			CodeStatementSwitch actionSwitch = new CodeStatementSwitch("actionId");
			serviceCase.addStatement(actionSwitch);

			for (ApiServiceMethod action : service.getServerActions()) {
				CodeMethod actionMethod = cls.addMethod(action.getName());
				actionMethod.getModifier().add(CodeModifier.INTERFACE);
				actionMethod.addThrows(engine.getClass("java.lang.Exception"));
				actionMethod.getArgument(action.hasArgumentName("client") ? "_client" : "client").setType(c);

				CodeMethod executeMethod = gate.addMethod("call_" + service.getName() + '_' + action.getName());
				CodeExpressionMethod executeCall = new CodeExpressionMethod(executeMethod.getName());
				executeMethod.getModifier().add(CodeModifier.PRIVATE);
				executeMethod.getArgument("client").setType(c);
				executeCall.addArgument("client");
				executeMethod.getArgument(reader.getWord()).setType(engine.getClass("org.shypl.biser.io.DataReader"));
				executeCall.addArgument(reader);
				if (action.hasResult() && !action.isResultDeferred()) {
					executeMethod.getArgument(writer.getWord()).setType(engine.getClass("org.shypl.biser.io.DataWriter"));
					executeCall.addArgument(writer);
				}
				executeMethod.setReturnType(primitiveVoid);
				executeMethod.addThrows(engine.getClass("java.lang.Exception"));

				CodeStatementSwitchCase actionCase = actionSwitch.addCase(String.valueOf(action.getId()));
				actionCase.addStatement(executeCall)
					.addStatement(CodeStatementReturn.EMPTY);

				CodeStatementBlock executeBody = executeMethod.getBody();

				CodeExpressionMethod actionCall = new CodeExpressionMethod(new CodeExpressionWord("service" + service.getCamelName()), actionMethod.getName());
				actionCall.addArgument("client");
				DataType resultType = action.getResultType();

				CodeClass responseClass = null;
				if (action.hasResult()) {
					if (action.isResultDeferred()) {
						actionMethod.setReturnType(primitiveVoid);

						responseClass = pack.getClass(service.getCamelName() + Utils.convertToCamel(actionMethod.getName()) + "Response");
						responseClass.setParent(engine.getClass("org.shypl.biser.csi.server.DeferredResponse"));
						responseClass.getModifier().add(CodeModifier.FINAL | CodeModifier.PUBLIC);

						CodeMethod method = responseClass.addMethod(responseClass.getName());
						method.getArgument("client").setType(clientClass);
						method.getArgument("responseId").setType(primitiveInt);
						method.getBody().addStatement(new CodeExpressionMethod("super",
							new CodeExpressionWord("client"),
							new CodeExpressionWord("responseId"),
							new CodeExpressionString(service.getName()),
							new CodeExpressionString(action.getName())
						));

						method = responseClass.addMethod("send");
						method.getModifier().add(CodeModifier.PUBLIC);
						method.setReturnType(primitiveVoid);
						method.getArgument("result").setType(getType(resultType));

						method.getBody()
							.addStatement(new CodeExpressionMethod("_log", new CodeExpressionWord("result")))
							.addStatement(defineEncode(new CodeExpressionWord("result"), new CodeExpressionWord("_writer"), resultType))
							.addStatement(new CodeExpressionMethod("_send"));

						executeBody.addStatement(new CodeExpressionVar("response", responseClass).assign(new CodeExpressionNew(responseClass,
							new CodeExpressionWord("client"),
							new CodeExpressionMethod(reader, "readInt")
						)));
					}
					else {
						executeBody.addStatement(new CodeExpressionMethod(writer, "writeInt", new CodeExpressionMethod(reader, "readInt")));
						actionMethod.setReturnType(getType(resultType));
					}
				}
				else {
					actionMethod.setReturnType(primitiveVoid);
				}

				CodeExpressionMethod log = new CodeExpressionMethod("logCall",
					new CodeExpressionWord("client"),
					new CodeExpressionString(service.getName()),
					new CodeExpressionString(action.getName())
				);


				if (action.hasArguments()) {
					int i = 0;
					for (Parameter arg : action.getArguments()) {
						DataType type = arg.getType();
						CodeExpressionWord var = new CodeExpressionWord("arg" + (++i));
						executeBody.addStatement(defineDecode(new CodeExpressionVar(var.getWord(), getType(type)), reader, type));
						log.addArgument(var);
						actionMethod.getArgument(arg.getName()).setType(getType(type));
						actionCall.addArgument(var);
					}
				}

				executeBody.addStatement(log);

				if (action.hasResult()) {
					if (action.isResultDeferred()) {
						actionCall.addArgument(new CodeExpressionWord("response"));
						actionMethod.getArgument("response").setType(responseClass);
						executeBody.addStatement(actionCall);
					}
					else {
						executeBody.addStatement(new CodeExpressionVar("result", getType(resultType)).assign(actionCall));
						executeBody.addStatement(new CodeExpressionMethod("logResponse",
							new CodeExpressionWord("client"),
							new CodeExpressionString(service.getName()),
							new CodeExpressionString(action.getName()),
							new CodeExpressionWord("result")
						));
						executeBody.addStatement(defineEncode(new CodeExpressionWord("result"), writer, resultType));
					}
				}
				else {
					executeBody.addStatement(actionCall);
				}
			}

			return cls;
		}

		/*
		private class LogMethod extends CodeExpressionMethod {
			private final List<Arg> args = new ArrayList<>();
			private       boolean   raw  = true;

			public LogMethod(CodeExpression target, String method, int type, ApiService service, ApiServiceMethod action, int argumentsSize, Arg... args) {
				super(target, method, createActionLogMessage(type, service, action, argumentsSize));
				Collections.addAll(this.args, args);
			}

			public LogMethod(String method, int type, ApiService service, ApiServiceMethod action, int argumentsSize, Arg... args) {
				super(method, createActionLogMessage(type, service, action, argumentsSize));
				Collections.addAll(this.args, args);
			}

			public void addArgument(String argument, DataType type) {
				addArgument(new CodeExpressionWord(argument), type);
			}

			public void addArgument(CodeExpression argument, DataType type) {
				args.add(new Arg(argument, type));
			}

			@Override
			public void visit(CodeVisitor visitor) {
				if (raw) {
					raw = false;
					if (args.size() == 1
						&& (args.get(0).type instanceof ArrayType)
						&& !(((ArrayType)args.get(0).type).getElementType() instanceof PrimitiveType)
						) {
						Arg arg = args.get(0);
						arg.argument = new CodeExpressionNew(engine.getClass("org.shypl.common.util.VarargsObjectArray"), arg.argument);
					}
					for (Arg arg : args) {
						addArgument(arg.argument);
					}
				}
				super.visit(visitor);
			}
		}

		*/
	}
}
