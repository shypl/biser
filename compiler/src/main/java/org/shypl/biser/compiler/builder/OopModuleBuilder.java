package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.CompilerException;
import org.shypl.biser.compiler.Module;
import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.model.Api;
import org.shypl.biser.compiler.model.EntityType;
import org.shypl.biser.compiler.model.EnumType;
import org.shypl.biser.compiler.model.Model;
import org.shypl.biser.compiler.model.StructureType;

import java.io.File;
import java.nio.file.Path;

public abstract class OopModuleBuilder implements ModuleBuilder {
	private final String lang;
	private final String codeFileExtension;

	protected OopModuleBuilder(String lang, String codeFileExtension) {
		this.lang = lang;
		this.codeFileExtension = codeFileExtension;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public void build(Module module, Model model) throws CompilerException {
		OopCodeBuilder codeBuilder = createCodeBuilder(module.getPackage());

		for (StructureType type : model.getStructures()) {
			if (type instanceof EntityType) {
				codeBuilder.buildEntity((EntityType)type);
			}
			else if (type instanceof EnumType) {
				codeBuilder.buildEnum((EnumType)type);
			}
		}

		for (Api gate : model.getApis()) {
			if (module.hasApi(gate.getName())) {
				switch (module.getApiSide(gate.getName())) {
					case CLIENT:
						codeBuilder.buildClientApi(gate);
						break;
					case SERVER:
						codeBuilder.buildServerApi(gate);
						break;
				}
			}
		}

		for (CodeClass cls : codeBuilder.getClasses()) {
			OopCodeFile file = createCodeFile();

			file.writeMainClass(cls);

			Path path = module.getTarget()
				.resolve(cls.getPackage().getFullName(File.separatorChar))
				.resolve(cls.getName() + '.' + codeFileExtension);

			file.save(path);
		}
	}

	protected abstract OopCodeBuilder createCodeBuilder(String pack);

	protected abstract OopCodeFile createCodeFile();
}
