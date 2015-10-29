package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeEngine;
import org.shypl.biser.compiler.code.CodeExpression;
import org.shypl.biser.compiler.code.CodeExpressionString;
import org.shypl.biser.compiler.code.CodePackage;
import org.shypl.biser.compiler.code.CodeType;
import org.shypl.biser.compiler.model.ApiAction;
import org.shypl.biser.compiler.model.ApiGate;
import org.shypl.biser.compiler.model.ApiService;
import org.shypl.biser.compiler.model.ArrayType;
import org.shypl.biser.compiler.model.DataType;
import org.shypl.biser.compiler.model.EntityType;
import org.shypl.biser.compiler.model.EnumType;
import org.shypl.biser.compiler.model.TypeRepresenter;

import java.util.Collection;

public abstract class OopCodeBuilder implements TypeRepresenter<CodeType> {
	protected final CodeEngine  engine;
	protected final CodePackage modulePackage;

	public OopCodeBuilder(CodeEngine engine, String modulePackage) {
		this.engine = engine;
		this.modulePackage = engine.getPackage(modulePackage);
	}

	public CodeType getType(DataType type) {
		return type.represent(this);
	}

	@Override
	public CodeType representEntity(EntityType type) {
		return modulePackage.getClass(type.getName());
	}

	@Override
	public CodeType representEnum(EnumType type) {
		return modulePackage.getClass(type.getName());
	}

	@Override
	public CodeType representArray(ArrayType type) {
		return engine.getArray(getType(type.getElementType()));
	}

	public Collection<CodeClass> getClasses() {
		return modulePackage.getAllClasses();
	}

	public abstract void buildServerApi(ApiGate gate);

	public abstract void buildClientApi(ApiGate gate);

	public abstract void buildEnum(EnumType type);

	public abstract void buildEntity(EntityType type);

	public abstract class SubPackageBuilder {
		public static final int ACTION_LOG_SERVER          = 0;
		public static final int ACTION_LOG_CLIENT          = 1;
		public static final int ACTION_LOG_SERVER_RESPONSE = 2;
		public static final int ACTION_LOG_CLIENT_GLOBAL   = 3;
		protected final CodePackage pack;

		public SubPackageBuilder(String pack) {
			this.pack = modulePackage.getPackage(pack);
		}

		protected CodeExpression createActionLogMessage(int type, ApiService service, ApiAction action, int argumentsSize) {
			StringBuilder logMessage = new StringBuilder();

			switch (type) {
				case ACTION_LOG_SERVER:
					logMessage.append("> ");
					break;
				case ACTION_LOG_CLIENT:
					logMessage.append("< ");
					break;
				case ACTION_LOG_SERVER_RESPONSE:
					logMessage.append("< ");
					break;
				case ACTION_LOG_CLIENT_GLOBAL:
					logMessage.append("<< ");
					break;
			}


			logMessage.append(pack.getName())
				.append('.')
				.append(service.getName())
				.append('.')
				.append(action.getName());

			if (type == ACTION_LOG_SERVER_RESPONSE) {
				logMessage.append(": ");
			}
			else {
				logMessage.append('(');
			}

			for (int i = 0; i < argumentsSize; ++i) {
				if (i > 0) {
					logMessage.append(", ");
				}
				logMessage.append("{}");
			}

			if (type != ACTION_LOG_SERVER_RESPONSE) {
				logMessage.append(')');
			}

			return new CodeExpressionString(logMessage.toString());
		}
	}
}
