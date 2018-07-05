package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodeEngine;
import org.shypl.biser.compiler.code.CodePackage;
import org.shypl.biser.compiler.code.CodeType;
import org.shypl.biser.compiler.model.Api;
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

	public abstract void buildServerApi(Api api);

	public abstract void buildClientApi(Api api);

	public abstract void buildEnum(EnumType type);

	public abstract void buildEntity(EntityType type);

	public abstract class SubPackageBuilder {
		protected final String      name;
		protected final CodePackage pack;

		public SubPackageBuilder(String name) {
			this.name = name;
			this.pack = modulePackage.getPackage(name);
		}
	}
}
