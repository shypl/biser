package org.shypl.biser.compiler.builder.kotlin.js;

import org.shypl.biser.compiler.builder.OopCodeBuilder;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeEngine;
import org.shypl.biser.compiler.code.CodeExpression;
import org.shypl.biser.compiler.code.CodeExpressionAssign;
import org.shypl.biser.compiler.code.CodeExpressionField;
import org.shypl.biser.compiler.code.CodeExpressionLambda;
import org.shypl.biser.compiler.code.CodeExpressionMethod;
import org.shypl.biser.compiler.code.CodeExpressionNew;
import org.shypl.biser.compiler.code.CodeExpressionString;
import org.shypl.biser.compiler.code.CodeExpressionStringConcat;
import org.shypl.biser.compiler.code.CodeExpressionVar;
import org.shypl.biser.compiler.code.CodeExpressionWord;
import org.shypl.biser.compiler.code.CodeMethod;
import org.shypl.biser.compiler.code.CodeModifier;
import org.shypl.biser.compiler.code.CodeParameter;
import org.shypl.biser.compiler.code.CodeParametrizedClass;
import org.shypl.biser.compiler.code.CodePrimitive;
import org.shypl.biser.compiler.code.CodeStatementBlock;
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
public class KotlinJsCodeBuilder extends OopCodeBuilder {
	private final Map<PrimitiveType, CodeType> primitives          = new HashMap<>();
	private final Map<PrimitiveType, String>   primitiveCamelNames = new HashMap<>();
	private final Map<PrimitiveType, String>   constPrimitives      = new HashMap<>();
	
	private final Map<DataType, CodeExpression> typeDefaults = new HashMap<>();
	
	private final TypeEncoderDefender typeEncoderDefender;
	private final TypeDecoderDefender typeDecoderDefender;
	
	private final CodePrimitive primitiveVoid;
	private final CodeClass     primitiveInt;
	private final CodeClass     primitiveString;
	
	public KotlinJsCodeBuilder(String pack) {
		super(new CodeEngine('.'), pack);
		
		typeDecoderDefender = new TypeDecoderDefender();
		typeEncoderDefender = new TypeEncoderDefender();
		
		primitiveVoid = engine.getPrimitive("Unit");
		primitiveInt = engine.getClass("Int");
		primitiveString = engine.getClass("String");
		
		primitives.put(PrimitiveType.BYTE, engine.getClass("Byte"));
		primitives.put(PrimitiveType.BOOL, engine.getClass("Boolean"));
		primitives.put(PrimitiveType.INT, primitiveInt);
		primitives.put(PrimitiveType.LONG, engine.getClass("Long"));
		primitives.put(PrimitiveType.DOUBLE, engine.getClass("Double"));
		primitives.put(PrimitiveType.STRING, primitiveString);
		primitives.put(PrimitiveType.BYTES, engine.getClass("ByteArray"));
		
		typeDefaults.put(PrimitiveType.BYTE, CodeExpressionWord._0);
		typeDefaults.put(PrimitiveType.BOOL, CodeExpressionWord.FALSE);
		typeDefaults.put(PrimitiveType.INT, CodeExpressionWord._0);
		typeDefaults.put(PrimitiveType.LONG, CodeExpressionWord._0);
		typeDefaults.put(PrimitiveType.DOUBLE, new CodeExpressionWord("0.0"));
		
		primitiveCamelNames.put(PrimitiveType.BYTE, "Byte");
		primitiveCamelNames.put(PrimitiveType.BOOL, "Bool");
		primitiveCamelNames.put(PrimitiveType.INT, "Int");
		primitiveCamelNames.put(PrimitiveType.LONG, "Long");
		primitiveCamelNames.put(PrimitiveType.DOUBLE, "Double");
		primitiveCamelNames.put(PrimitiveType.STRING, "String");
		primitiveCamelNames.put(PrimitiveType.BYTES, "Bytes");
		
		constPrimitives.put(PrimitiveType.BYTE, "BYTE");
		constPrimitives.put(PrimitiveType.BOOL, "BOOLEAN");
		constPrimitives.put(PrimitiveType.INT, "INT");
		constPrimitives.put(PrimitiveType.LONG, "LONG");
		constPrimitives.put(PrimitiveType.DOUBLE, "DOUBLE");
		constPrimitives.put(PrimitiveType.STRING, "STRING");
		constPrimitives.put(PrimitiveType.BYTES, "BYTES");
	}
	
	@Override
	public CodeType representPrimitive(PrimitiveType type) {
		return primitives.get(type);
	}
	
	@Override
	public CodeType representMap(MapType type) {
		throw new IllegalStateException("Not supported");
	}
	
	@Override
	public void buildEntity(EntityType type) {
		CodeClass cls = modulePackage.getClass(type.getName());
		
		if (type.hasParent()) {
			cls.setParent(cls.getPackage().getClass(type.getParent().getName()));
		}
		else {
			cls.setParent(engine.getClass("org.shypl.biser.io.Entity"));
		}
		
		// fields
		for (Parameter typeField : type.getFields()) {
			CodeParameter classField = cls.getField(typeField.getName());
			classField.setType(getType(typeField.getType()));
			if (typeDefaults.containsKey(typeField.getType())) {
				classField.setValue(typeDefaults.get(typeField.getType()));
			}
		}
		
		cls.addMethod("constructor");
		
		CodeMethod method;
		CodeStatementBlock methodBody;
		
		// biser id
		{
			CodeParameter fieldClassId = cls.getField("_ID");
			fieldClassId.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.FINAL);
			fieldClassId.setType(primitiveInt);
			fieldClassId.setValue(new CodeExpressionWord(type.getId()));
			
			method = cls.addMethod("_id");
			method.getModifier().set(CodeModifier.PUBLIC | CodeModifier.OVERRIDE);
			method.setReturnType(primitiveInt);
			method.getBody().addStatement(new CodeStatementReturn("_ID"));
		}
		
		
		// biser decoder
		{
			CodeParameter decoder = cls.getField("_DECODER");
			decoder.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.FINAL);
			CodeParametrizedClass entityDecoderClass = engine.getClass("org.shypl.biser.io.Decoder").parametrize(cls);
			CodeExpressionLambda expressionLambda = new CodeExpressionLambda();
			decoder.setType(entityDecoderClass);
			decoder.setValue(new CodeExpressionMethod(engine.getClass("org.shypl.biser.io.Decoders"), "entity", expressionLambda));
			
			
			methodBody = expressionLambda.getBody();
			
			if (type.hasChildren()) {
				CodeStatementSwitch swt = new CodeStatementSwitch(new CodeExpressionWord("it"));
				swt.getDefaultCase().addStatement(new CodeStatementThrow("IllegalArgumentException()"));
				swt.addCase(new CodeExpressionField(cls, "_ID"))
					.addStatement(new CodeExpressionNew(cls));
				for (EntityType childType : type.getChildren()) {
					swt.addCase(new CodeExpressionField(modulePackage.getClass(childType.getName()), "_ID"))
						.addStatement(new CodeExpressionNew(getType(childType)));
				}
				methodBody.addStatement(swt);
			}
			else {
				methodBody.addStatement(new CodeExpressionNew(cls));
			}
			
		}
		
		// biser encode decode
		if (type.hasFields()) {
			method = cls.addMethod("_encode");
			method.getModifier().set(CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			method.getArgument("writer").setType(engine.getClass("org.shypl.biser.io.DataWriter"));
			CodeStatementBlock bodyEncode = method.getBody();
			
			method = cls.addMethod("_decode");
			method.getModifier().set(CodeModifier.OVERRIDE);
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
		if (type.hasFields()) {
			method = cls.addMethod("_toString");
			method.getModifier().set(CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			method.getArgument("fields").setType(engine.getClass("MutableMap").parametrize(primitiveString, primitiveString));
			methodBody = method.getBody();
			CodeExpressionWord fields = new CodeExpressionWord("fields");
			
			if (type.hasParent()) {
				methodBody.addStatement(new CodeExpressionMethod("super._toString", fields));
			}
			
			CodeClass stringUtils = engine.getClass("org.shypl.biser.StringUtils");
			
			for (Parameter field : type.getFields()) {
				methodBody.addStatement(fields.method("put",
					new CodeExpressionString(field.getName()), stringUtils.method("toString", CodeExpressionWord.THIS.field(field.getName()))));
			}
		}
	}
	
	@Override
	public void buildEnum(EnumType type) {
		CodeClass cls = modulePackage.getClass(type.getName());
		cls.getModifier().set(CodeModifier.PUBLIC);
		cls.addEnumValues(type.getValues());
		
		CodeParameter field = cls.getField("_DECODER");
		field.getModifier().set(CodeModifier.PUBLIC | CodeModifier.STATIC | CodeModifier.FINAL);
		field.setType(engine.getClass("org.shypl.biser.io.Decoder").parametrize(cls));
		field.setValue(new CodeExpressionMethod(engine.getClass("org.shypl.biser.io.Decoders"), "enum"));
	}
	
	@Override
	public void buildServerApi(Api api) {
		throw new IllegalStateException("Not supported");
	}
	
	@Override
	public void buildClientApi(Api api) {
		ClientCsiBuilder builder = new ClientCsiBuilder();
		builder.buildServer(api.getServerServices());
		builder.buildClient(api.getClientServices());
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
			return writer.method("writeList", target, type.getElementType().represent(encoder));
		}
		
		@Override
		public CodeExpression representMap(MapType type) {
			throw new UnsupportedOperationException();
		}
		
		private class TypeEncoder implements TypeRepresenter<CodeExpression> {
			private final CodeClass encoder = engine.getClass("org.shypl.biser.io.Encoders");
			
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
				return encoder.method("list", elementType.represent(this));
			}
			
			@Override
			public CodeExpression representMap(MapType type) {
				throw new UnsupportedOperationException();
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
			return reader.method("readList", type.getElementType().represent(decoder));
		}
		
		@Override
		public CodeExpression representMap(MapType type) {
			throw new UnsupportedOperationException();
		}
		
		private CodeExpression decode(DataType type) {
			return type.represent(decoder).method("invoke", reader);
		}
		
		private class TypeDecoder implements TypeRepresenter<CodeExpression> {
			private final CodeClass decoder = engine.getClass("org.shypl.biser.io.Decoders");
			
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
				
				return decoder.method("list", elementType.represent(this));
			}
			
			@Override
			public CodeExpression representMap(MapType type) {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	private class ClientCsiBuilder extends SubPackageBuilder {
		
		private final CodeClass apiClass;
		
		public ClientCsiBuilder() {
			super("api");
			apiClass = pack.getClass("Api");
			apiClass.setParent(engine.getClass("org.shypl.biser.csi.client.AbstractApi"));
			apiClass.getModifier().add(CodeModifier.PUBLIC);
		}
		
		public void buildServer(List<ApiService> services) {
			CodeClass cls = apiClass;
			
			if (!services.isEmpty()) {
				buildServerServices(services);
				
				CodeParameter server = cls.getField("server");
				server.getModifier().add(CodeModifier.FINAL);
				server.setType(pack.getClass("ServerServices"));
				server.setValue(new CodeExpressionNew(pack.getClass("ServerServices"), CodeExpressionWord.THIS));
			}
		}
		
		public void buildClient(List<ApiService> services) {
			CodeClass cls = apiClass;
			
			cls.addMethod("constructor");
			
			CodeMethod method = cls.addMethod("callService");
			method.getModifier().add(CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			method.getArgument("serviceId").setType(primitiveInt);
			method.getArgument("actionId").setType(primitiveInt);
			method.getArgument("reader").setType(engine.getClass("org.shypl.biser.io.DataReader"));
			
			if (!services.isEmpty()) {
				CodeStatementSwitch serviceSwitch = new CodeStatementSwitch(new CodeExpressionWord("serviceId"));
				serviceSwitch.getDefaultCase().addStatement(new CodeStatementThrow(new CodeExpressionNew(
					engine.getClass("IllegalArgumentException"), new CodeExpressionStringConcat(
					new CodeExpressionString("Action not exists: " + pack.getName() + ".#"),
					new CodeExpressionWord("serviceId"),
					new CodeExpressionString(".#"),
					new CodeExpressionWord("actionId"))
				)));
				
				method.getBody().addStatement(serviceSwitch);
				
				for (ApiService service : services) {
					CodeStatementSwitchCase serviceCase = serviceSwitch.addCase(String.valueOf(service.getId()));
					CodeClass serviceClass = buildClientService(cls, serviceCase, service);
					
					CodeParameter field = cls.getField("_service" + service.getCamelName());
					field.getModifier().add(CodeModifier.PRIVATE);
					field.setType(serviceClass);
					
					method = cls.addMethod("setService" + service.getCamelName());
					method.getModifier().set(CodeModifier.PUBLIC);
					method.setReturnType(primitiveVoid);
					method.getArgument("service").setType(serviceClass);
					
					method.getBody()
						.addStatement(CodeExpressionWord.THIS.field(field.getName()).assign(new CodeExpressionWord("service")));
				}
			}
			
		}
		
		private void buildServerServices(List<ApiService> services) {
			CodeClass cls = pack.getClass("ServerServices");
			cls.getModifier().add(CodeModifier.PUBLIC);
			CodeMethod method = cls.addMethod("constructor");
			method.getModifier().add(CodeModifier.PUBLIC);
			method.getArgument("api").setType(apiClass);
			CodeStatementBlock body = method.getBody();
			
			CodeExpressionWord api = new CodeExpressionWord("api");
			for (ApiService service : services) {
				CodeClass serviceClass = buildServerService(service);
				CodeParameter field = cls.getField( service.getName());
				field.setType(serviceClass);
				field.getModifier().add(CodeModifier.FINAL);
				body.addStatement(new CodeExpressionAssign(field.getVariable(), new CodeExpressionNew(serviceClass, api)));
			}
		}
		
		private CodeClass buildServerService(ApiService service) {
			CodeClass cls = pack.getClass("ServerService" + service.getCamelName());
			cls.getModifier().add(CodeModifier.PUBLIC);
			cls.setParent(engine.getClass("org.shypl.biser.csi.client.ServerService"));
			
			CodeMethod method = cls.addMethod("constructor");
			method.getModifier().add(CodeModifier.PUBLIC);
			method.getArgument("api").setType(apiClass);
			method.getBody()
				.addStatement(new CodeExpressionMethod("construct",
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
			field.getModifier().add(CodeModifier.PRIVATE | CodeModifier.FINAL);
			
			CodeMethod method = holderClass.addMethod("constructor");
			method.getModifier().add(CodeModifier.PUBLIC);
			CodeParameter arg = method.getArgument("handler");
			arg.setType(cls);
			method.getBody().addStatement(field.getVariable().assign(arg.getVariable()));
			
			method = holderClass.addMethod("process");
			method.getModifier().add(CodeModifier.OVERRIDE);
			method.setReturnType(primitiveVoid);
			
			CodeParameter reader = method.getArgument("reader");
			reader.setType(engine.getClass("org.shypl.biser.io.DataReader"));
			
			CodeExpressionVar result = new CodeExpressionVar("result", getType(action.getResultType()));
			
			method.getBody()
				.addStatement(defineDecode(result, reader.getVariable(), action.getResultType()))
				.addStatement(new CodeExpressionMethod("log",
					new CodeExpressionString(service.getName()), new CodeExpressionString(action.getName()), result.getVariable()))
				.addStatement(field.getVariable().method(handleResult.getName(), result.getVariable()));
			
			return cls;
		}
		
		private CodeClass buildClientService(CodeClass api, CodeStatementSwitchCase serviceCase, ApiService service) {
			CodeClass cls = pack.getClass("Service" + service.getCamelName());
			cls.setInterface(true);
			cls.getModifier().add(CodeModifier.PUBLIC);
			
			CodeStatementSwitch actionSwitch = new CodeStatementSwitch(new CodeExpressionWord("actionId"));
			actionSwitch.getDefaultCase().addStatement(new CodeStatementThrow(new CodeExpressionNew(
				engine.getClass("IllegalArgumentException"), new CodeExpressionStringConcat(
				new CodeExpressionString("Action not exists: " + pack.getName() + ".#"),
				new CodeExpressionWord("serviceId"),
				new CodeExpressionString(".#"),
				new CodeExpressionWord("actionId"))
			)));
			
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
				
				
			}
			
			return cls;
		}
		
		private void buildClientServiceAction(ApiService service, ApiServiceMethod action, CodeMethod executeMethod, CodeMethod actionMethod,
			CodeExpressionWord reader
		)
		{
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
