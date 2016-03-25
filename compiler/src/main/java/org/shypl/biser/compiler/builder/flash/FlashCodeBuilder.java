package org.shypl.biser.compiler.builder.flash;

import org.shypl.biser.compiler.Utils;
import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeEngine;
import org.shypl.biser.compiler.code.CodeExpression;
import org.shypl.biser.compiler.code.CodeExpressionAssign;
import org.shypl.biser.compiler.code.CodeExpressionBinaryOperator;
import org.shypl.biser.compiler.code.CodeExpressionCallClass;
import org.shypl.biser.compiler.code.CodeExpressionField;
import org.shypl.biser.compiler.code.CodeExpressionMethod;
import org.shypl.biser.compiler.code.CodeExpressionNew;
import org.shypl.biser.compiler.code.CodeExpressionString;
import org.shypl.biser.compiler.code.CodeExpressionStringConcat;
import org.shypl.biser.compiler.code.CodeExpressionTernaryOperator;
import org.shypl.biser.compiler.code.CodeExpressionVar;
import org.shypl.biser.compiler.code.CodeExpressionWord;
import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.CodeModifier;
import org.shypl.biser.compiler.code.CodeParameter;
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
public class FlashCodeBuilder extends OopCodeBuilder {
	private final Map<PrimitiveType, CodeType> primitives          = new HashMap<>();
	private final Map<PrimitiveType, String>   primitiveCamelNames = new HashMap<>();

	private final TypeEncoderDefender typeEncoderDefender;
	private final TypeDecoderDefender typeDecoderDefender;
	private final NullValueDefender   nullValueDefender;

	private final CodePrimitive primitiveVoid;
	private final CodeClass     primitiveInt;
	private final CodeClass     primitiveString;

	public FlashCodeBuilder(String pack) {
		super(new CodeEngine('.'), pack);

		typeDecoderDefender = new TypeDecoderDefender();
		typeEncoderDefender = new TypeEncoderDefender();
		nullValueDefender = new NullValueDefender();

		primitiveVoid = engine.getPrimitive("void");
		primitiveInt = engine.getClass("int");
		primitiveString = engine.getClass("String");

		primitives.put(PrimitiveType.BYTE, primitiveInt);
		primitives.put(PrimitiveType.BOOL, engine.getClass("Boolean"));
		primitives.put(PrimitiveType.INT, primitiveInt);
		primitives.put(PrimitiveType.UINT, engine.getClass("uint"));
		primitives.put(PrimitiveType.LONG, engine.getClass("org.shypl.common.math.Long"));
		primitives.put(PrimitiveType.ULONG, engine.getClass("org.shypl.common.math.Long"));
		primitives.put(PrimitiveType.DOUBLE, engine.getClass("Number"));
		primitives.put(PrimitiveType.STRING, primitiveString);
		primitives.put(PrimitiveType.DATE, engine.getClass("Date"));
		primitives.put(PrimitiveType.BYTES, engine.getClass("flash.utils.ByteArray"));

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
		return engine.getClass("org.shypl.common.collection.Map");
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

		CodeStatementBlock methodBody;
		CodeMethod method;

		if (type.hasAllFields()) {
			method = cls.addMethod(cls.getName());
			method.getModifier().set(CodeModifier.PUBLIC);

			for (Parameter field : type.getAllFields()) {
				CodeParameter argument = method.getArgument(field.getName());
				argument.setType(getType(field.getType()));
				argument.setValue(defineNullValue(field.getType()));
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
				DataType type1 = field.getType();
				if (type1 == PrimitiveType.LONG || type1 == PrimitiveType.ULONG) {
					methodBody.addStatement(CodeExpressionWord.THIS.field(field.getName()).assign(new CodeExpressionTernaryOperator(
						new CodeExpressionBinaryOperator("===", new CodeExpressionWord(field.getName()), CodeExpressionWord.NULL),
						new CodeExpressionField(engine.getClass("org.shypl.common.math.Long"), "ZERO"),
						new CodeExpressionWord(field.getName())
					)));
				}
				else {
					methodBody.addStatement(CodeExpressionWord.THIS.field(field.getName()).assign(field.getName()));
				}
			}
		}

		// biser decoder
		CodeClass entityDecoderClass = modulePackage.getClass(type.getName() + "_Decoder");
		entityDecoderClass.getModifier().add(CodeModifier.INTERNAL);
		entityDecoderClass.setParent(engine.getClass("org.shypl.biser.io.EntityDecoder"));

		CodeParameter decoder = cls.getField("_DECODER");
		decoder.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.CONST);
		decoder.setType(engine.getClass("org.shypl.biser.io.Decoder"));
		decoder.setValue(new CodeExpressionNew(entityDecoderClass, cls));

		method = entityDecoderClass.addMethod(entityDecoderClass.getName());
		method.getModifier().set(CodeModifier.PUBLIC);
		method.getArgument("type").setType(engine.getClass("Class"));
		method.getBody().addStatement(new CodeExpressionMethod("super", new CodeExpressionWord("type")));

		method = entityDecoderClass.addMethod("factory");
		method.getModifier().set(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
		method.setReturnType(engine.getClass("org.shypl.biser.io.Entity"));
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
		method.getArgument("fields").setType(engine.getClass("org.shypl.common.collection.Map"));
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

		cls.setParent(engine.getClass("org.shypl.common.lang.Enum"));

		for (String value : type.getValues()) {
			CodeParameter field = cls.getField(value);
			field.setType(cls);
			field.getModifier().add(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.CONST);
			field.setValue(new CodeExpressionNew(cls, new CodeExpressionString(value)));
		}

		CodeParameter field = cls.getField("_DECODER");
		field.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.CONST);
		field.setType(engine.getClass("org.shypl.biser.io.Decoder"));
		field.setValue(new CodeExpressionNew(engine.getClass("org.shypl.biser.io.EnumDecoder"), cls));

		CodeMethod method = cls.addMethod(cls.getName());
		method.getModifier().add(CodeModifier.PUBLIC);
		method.getArgument("name").setType(primitiveString);
		method.getBody().addStatement(new CodeExpressionMethod("super", new CodeExpressionWord("name")));
	}

	@Override
	public void buildServerApi(Api api) {
		throw new IllegalStateException("Not supported");
	}

	@Override
	public void buildClientApi(Api api) {
		ClientCsiBuilder builder = new ClientCsiBuilder(api.getName());
		builder.buildServer(api.getServerServices());
		builder.buildClient(api.getClientServices());
	}

	private CodeExpression defineNullValue(DataType type) {
		return type.represent(nullValueDefender);
	}

	private CodeExpression defineEncode(CodeExpression target, CodeExpression writer, DataType type) {
		return typeEncoderDefender.define(target, writer, type);
	}

	private CodeExpression defineDecode(CodeExpression target, CodeExpression reader, DataType type) {
		return typeDecoderDefender.define(target, reader, type);
	}

	private class NullValueDefender implements TypeRepresenter<CodeExpression> {

		private final Map<PrimitiveType, CodeExpression> primitives = new HashMap<>();

		public NullValueDefender() {
			primitives.put(PrimitiveType.BYTE, CodeExpressionWord._0);
			primitives.put(PrimitiveType.BOOL, CodeExpressionWord.FALSE);
			primitives.put(PrimitiveType.INT, CodeExpressionWord._0);
			primitives.put(PrimitiveType.UINT, CodeExpressionWord._0);
			primitives.put(PrimitiveType.LONG, CodeExpressionWord.NULL);
			primitives.put(PrimitiveType.ULONG, CodeExpressionWord.NULL);
			primitives.put(PrimitiveType.DOUBLE, CodeExpressionWord._0);
			primitives.put(PrimitiveType.STRING, CodeExpressionWord.NULL);
			primitives.put(PrimitiveType.DATE, CodeExpressionWord.NULL);
			primitives.put(PrimitiveType.BYTES, CodeExpressionWord.NULL);
		}

		@Override
		public CodeExpression representPrimitive(PrimitiveType type) {
			return primitives.get(type);
		}

		@Override
		public CodeExpression representEntity(EntityType type) {
			return CodeExpressionWord.NULL;
		}

		@Override
		public CodeExpression representEnum(EnumType type) {
			return CodeExpressionWord.NULL;
		}

		@Override
		public CodeExpression representArray(ArrayType type) {
			return CodeExpressionWord.NULL;
		}

		@Override
		public CodeExpression representMap(MapType type) {
			return CodeExpressionWord.NULL;
		}
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
			return writer.method("writeArray", target, type.getElementType().represent(encoder));
		}

		@Override
		public CodeExpression representMap(MapType type) {
			return writer.method("writeMap", target, type.getKeyType().represent(encoder), type.getValueType().represent(encoder));
		}

		private class TypeEncoder implements TypeRepresenter<CodeExpression> {

			@Override
			public CodeExpression representPrimitive(PrimitiveType type) {
				return engine.getClass("org.shypl.biser.io." + primitiveCamelNames.get(type) + "Encoder").field("INSTANCE");
			}

			@Override
			public CodeExpression representEntity(EntityType type) {
				return engine.getClass("org.shypl.biser.io.EntityEncoder").field("INSTANCE");
			}

			@Override
			public CodeExpression representEnum(EnumType type) {
				return engine.getClass("org.shypl.biser.io.EnumEncoder").field("INSTANCE");
			}

			@Override
			public CodeExpression representArray(ArrayType type) {
				return engine.getClass("org.shypl.biser.io.ArrayEncoder").method("factory", type.getElementType().represent(this));
			}

			@Override
			public CodeExpression representMap(MapType type) {
				return engine.getClass("org.shypl.biser.io.MapEncoder")
					.method("factory", type.getKeyType().represent(this), type.getValueType().represent(this));
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
			return new CodeExpressionCallClass(getType(type), decode(type));
		}

		@Override
		public CodeExpression representEnum(EnumType type) {
			return new CodeExpressionCallClass(getType(type), decode(type));
		}

		@Override
		public CodeExpression representArray(ArrayType type) {
			return new CodeExpressionBinaryOperator("as", reader.method("readArray", type.getElementType().represent(decoder)), getType(type));
		}

		@Override
		public CodeExpression representMap(MapType type) {
			return new CodeExpressionCallClass(engine.getClass("org.shypl.common.collection.Map"),
				reader.method("readMap", type.getKeyType().represent(decoder), type.getValueType().represent(decoder))
			);
		}

		private CodeExpression decode(DataType type) {
			return type.represent(decoder).method("decode", reader);
		}

		private class TypeDecoder implements TypeRepresenter<CodeExpression> {
			@Override
			public CodeExpression representPrimitive(PrimitiveType type) {
				return engine.getClass("org.shypl.biser.io." + primitiveCamelNames.get(type) + "Decoder").field("INSTANCE");
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
				return engine.getClass("org.shypl.biser.io.ArrayDecoder").method("factory", type.getElementType().represent(this));
			}

			@Override
			public CodeExpression representMap(MapType type) {
				return engine.getClass("org.shypl.biser.io.MapDecoder")
					.method("factory", type.getKeyType().represent(this), type.getValueType().represent(this));
			}
		}
	}

	private class ClientCsiBuilder extends SubPackageBuilder {

		private final CodeClass apiClass;

		public ClientCsiBuilder(String apiName) {
			super(apiName);
			apiClass = pack.getClass(Utils.convertToCamel(apiName) + "Api");
		}

		public void buildServer(List<ApiService> services) {
			CodeClass cls = apiClass;

			if (!services.isEmpty()) {
				buildServerServices(services);

				CodeParameter server = cls.getField("_server");
				server.setType(pack.getClass("ServerServices"));
				server.getModifier().add(CodeModifier.PRIVATE);

				CodeMethod method = cls.addMethod(cls.getName());
				method.getModifier().add(CodeModifier.PUBLIC);
				method.getBody()
					.addStatement(new CodeExpressionMethod("super", new CodeExpressionString(name)))
					.addStatement(
						new CodeExpressionWord(server.getName()).assign(new CodeExpressionNew(pack.getClass("ServerServices"), CodeExpressionWord.THIS)));

				method = cls.addMethod("server");
				method.getModifier().add(CodeModifier.PUBLIC | CodeModifier.GETTER | CodeModifier.FINAL);
				method.setReturnType(pack.getClass("ServerServices"));
				method.getBody().addStatement(new CodeStatementReturn(server.getName()));
			}
		}

		public void buildClient(List<ApiService> services) {
			CodeClass cls = apiClass;
			cls.setParent(engine.getClass("org.shypl.biser.csi.client.Api"));
			cls.getModifier().add(CodeModifier.PUBLIC);

			CodeMethod method = cls.addMethod("callService");
			method.getModifier().add(CodeModifier.PROTECTED | CodeModifier.FINAL | CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			method.getArgument("serviceId").setType(primitiveInt);
			method.getArgument("actionId").setType(primitiveInt);
			method.getArgument("reader").setType(engine.getClass("org.shypl.biser.io.DataReader"));

			CodeStatementSwitch serviceSwitch = new CodeStatementSwitch(new CodeExpressionWord("serviceId"));
			method.getBody()
				.addStatement(serviceSwitch)
				.addStatement(new CodeStatementThrow(new CodeExpressionNew(
					engine.getClass("org.shypl.biser.csi.CsiException"), new CodeExpressionStringConcat(
					new CodeExpressionString("Action not exists: " + pack.getName() + ".#"),
					new CodeExpressionWord("serviceId"),
					new CodeExpressionString(".#"),
					new CodeExpressionWord("actionId"))
				)));

			for (ApiService service : services) {
				CodeStatementSwitchCase serviceCase = serviceSwitch.addCase(String.valueOf(service.getId()));
				CodeClass serviceClass = buildClientService(cls, serviceCase, service);

				serviceCase.addStatement(CodeStatementBreak.INSTANCE);

				CodeParameter field = cls.getField("_service" + service.getCamelName());
				field.getModifier().add(CodeModifier.PRIVATE);
				field.setType(serviceClass);

				method = cls.addMethod("registerService" + service.getCamelName());
				method.getModifier().set(CodeModifier.PUBLIC | CodeModifier.FINAL);
				method.setReturnType(primitiveVoid);
				method.getArgument("service").setType(serviceClass);

//				CodeStatementIf statementIf = new CodeStatementIf(
//					new CodeExpressionBinaryOperator("!=", CodeExpressionWord.THIS.field(field.getName()), CodeExpressionWord.NULL));
//				statementIf.addStatement(new CodeStatementThrow(new CodeExpressionNew(engine.getClass("org.shypl.common.lang.RuntimeException"))));

				method.getBody()
//					.addStatement(statementIf)
					.addStatement(CodeExpressionWord.THIS.field(field.getName()).assign(new CodeExpressionWord("service")));
			}
		}

		private void buildServerServices(List<ApiService> services) {
			CodeClass cls = pack.getClass("ServerServices");
			cls.getModifier().add(CodeModifier.PUBLIC);
			CodeMethod method = cls.addMethod(cls.getName());
			method.getModifier().add(CodeModifier.FINAL | CodeModifier.PUBLIC);
			method.getArgument("api").setType(apiClass);
			CodeStatementBlock body = method.getBody();

			CodeExpressionWord api = new CodeExpressionWord("api");
			for (ApiService service : services) {
				CodeClass serviceClass = buildServerService(service);
				CodeParameter field = cls.getField("_" + service.getName());
				field.setType(serviceClass);
				field.getModifier().add(CodeModifier.PRIVATE);
				body.addStatement(new CodeExpressionAssign(field.getVariable(), new CodeExpressionNew(serviceClass, api)));

				method = cls.addMethod(service.getName());
				method.getModifier().add(CodeModifier.PUBLIC | CodeModifier.GETTER);
				method.setReturnType(serviceClass);
				method.getBody().addStatement(new CodeStatementReturn(field.getVariable()));
			}
		}

		private CodeClass buildServerService(ApiService service) {
			CodeClass cls = pack.getClass("ServerService" + service.getCamelName());
			cls.getModifier().add(CodeModifier.FINAL | CodeModifier.PUBLIC);
			cls.setParent(engine.getClass("org.shypl.biser.csi.client.ServerService"));

			CodeMethod method = cls.addMethod(cls.getName());
			method.getModifier().add(CodeModifier.PUBLIC);
			method.getArgument("api").setType(apiClass);
			method.getBody()
				.addStatement(new CodeExpressionMethod("super",
					new CodeExpressionWord("api"),
					new CodeExpressionWord(service.getId()),
					new CodeExpressionString(service.getName())
				));

			for (ApiServiceMethod action : service.getServerActions()) {
				buildServerServiceAction(service, action, cls);
			}

			return cls;
		}

		private void buildServerServiceAction(ApiService service, ApiServiceMethod action, CodeClass cls) {
			CodeMethod method = cls.addMethod(action.getName());
			method.getModifier().add(CodeModifier.PUBLIC);
			method.setReturnType(primitiveVoid);
			CodeStatementBlock body = method.getBody();

			CodeExpressionMethod log = new CodeExpressionMethod("_log", new CodeExpressionString(action.getName()));
			body.addStatement(log);
			CodeExpressionWord writer = new CodeExpressionWord("_writer");

			CodeExpressionMethod prepareMessage = new CodeExpressionMethod("_prepareMessage", new CodeExpressionWord(action.getId()));

			if (action.hasArguments()) {
				body.addStatement(new CodeExpressionVar(writer.getWord(), engine.getClass("org.shypl.biser.io.DataWriter")).assign(prepareMessage));
			}
			else {
				body.addStatement(prepareMessage);
			}

			for (Parameter arg : action.getArguments()) {
				method.getArgument(arg.getName()).setType(getType(arg.getType()));
				log.addArgument(arg.getName());
				body.addStatement(defineEncode(new CodeExpressionWord(arg.getName()), writer, arg.getType()));
			}

			if (action.hasResult()) {
				CodeClass rhClass = buildServerServiceActionResult(service, action);
				CodeParameter arg = method.getArgument(action.hasArgumentName("resultHandler") ? "_resultHandler" : "resultHandler");
				arg.setType(rhClass);
				prepareMessage.addArgument(new CodeExpressionNew(pack.getClass(rhClass.getName() + "_Holder"), arg.getVariable()));
			}

			body.addStatement(new CodeExpressionMethod("_sendMessage"));
		}

		private CodeClass buildServerServiceActionResult(ApiService service, ApiServiceMethod action) {
			CodeClass cls = pack.getClass(service.getCamelName() + action.getCamelName() + "ResultHandler");
			cls.getModifier().add(CodeModifier.PUBLIC);
			cls.setInterface(true);

			CodeMethod handleResult = cls.addMethod("handleResult" + service.getCamelName() + action.getCamelName());
			handleResult.getModifier().add(CodeModifier.INTERFACE);
			handleResult.getArgument("result").setType(getType(action.getResultType()));
			handleResult.setReturnType(primitiveVoid);

			CodeClass holderClass = pack.getClass(cls.getName() + "_Holder");
			holderClass.getModifier().add(CodeModifier.INTERNAL);
			holderClass.setParent(engine.getClass("org.shypl.biser.csi.client.ResultHandlerHolder"));

			CodeParameter field = holderClass.getField("_handler");
			field.setType(cls);
			field.getModifier().add(CodeModifier.PRIVATE);

			CodeMethod method = holderClass.addMethod(holderClass.getName());
			CodeParameter arg = method.getArgument("handler");
			arg.setType(cls);
			method.getBody().addStatement(field.getVariable().assign(arg.getVariable()));

			method = holderClass.addMethod("process");
			method.getModifier().add(CodeModifier.PROTECTED | CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);

			CodeParameter reader = method.getArgument("reader");
			reader.setType(engine.getClass("org.shypl.biser.io.DataReader"));

			CodeExpressionVar result = new CodeExpressionVar("result", getType(action.getResultType()));

			method.getBody()
				.addStatement(defineDecode(result, reader.getVariable(), action.getResultType()))
				.addStatement(new CodeExpressionMethod("log",
					new CodeExpressionString(service.getName()), new CodeExpressionString(action.getName()), result.getVariable()))
				.addStatement(field.getVariable().method(handleResult.getName(), result.getVariable()))
				.addStatement(field.getVariable().assign(CodeExpressionWord.NULL));

			return cls;
		}

		private CodeClass buildClientService(CodeClass api, CodeStatementSwitchCase serviceCase, ApiService service) {
			CodeClass cls = pack.getClass("Service" + service.getCamelName());
			cls.setInterface(true);
			cls.getModifier().add(CodeModifier.PUBLIC);

			CodeStatementSwitch actionSwitch = new CodeStatementSwitch(new CodeExpressionWord("actionId"));
			serviceCase.addStatement(actionSwitch);


			CodeExpressionWord reader = new CodeExpressionWord("reader");

			for (ApiServiceMethod action : service.getClientActions()) {
				CodeStatementSwitchCase actionCase = actionSwitch.addCase(String.valueOf(action.getId()));
				CodeMethod executeMethod = api.addMethod("call_" + service.getName() + "_" + action.getName());
				CodeMethod actionMethod = cls.addMethod(action.getName());

				if (action.hasResult()) {
					throw new RuntimeException("Not supported");
				}
				else {
					actionCase.addStatement(new CodeExpressionMethod(executeMethod.getName(), reader));
				}
				buildClientServiceAction(service, action, executeMethod, actionMethod, reader);

				actionCase.addStatement(CodeStatementReturn.EMPTY);
			}

			return cls;
		}

		private void buildClientServiceAction(ApiService service, ApiServiceMethod action, CodeMethod executeMethod, CodeMethod actionMethod,
			CodeExpressionWord reader
		) {
			executeMethod.getModifier().add(CodeModifier.PRIVATE);
			executeMethod.getArgument(reader.getWord()).setType(engine.getClass("org.shypl.biser.io.DataReader"));
			executeMethod.setReturnType(primitiveVoid);

			actionMethod.getModifier().add(CodeModifier.INTERFACE);
			actionMethod.setReturnType(primitiveVoid);

			CodeStatementBlock executeBody = executeMethod.getBody();
			CodeExpressionMethod logRequest = new CodeExpressionMethod("logCall",
				new CodeExpressionString(service.getName()),
				new CodeExpressionString(action.getName()));
			CodeExpressionMethod actionCall = new CodeExpressionMethod(new CodeExpressionWord("_service" + service.getCamelName()), actionMethod.getName());

			if (action.hasArguments()) {
				int i = 0;
				for (Parameter arg : action.getArguments()) {
					DataType type = arg.getType();
					CodeExpressionWord var = new CodeExpressionWord("arg" + (++i));
					executeBody.addStatement(defineDecode(new CodeExpressionVar(var.getWord(), getType(type)), reader, type));
					logRequest.addArgument(var);
					actionMethod.getArgument(arg.getName()).setType(getType(type));
					actionCall.addArgument(var);
				}
			}

			executeBody.addStatement(logRequest);
			executeBody.addStatement(actionCall);
		}
	}
}
