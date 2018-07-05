package org.shypl.biser.compiler.builder.cpp;

import org.shypl.biser.compiler.CompilerException;
import org.shypl.biser.compiler.Module;
import org.shypl.biser.compiler.builder.ModuleBuilder;
import org.shypl.biser.compiler.model.EntityType;
import org.shypl.biser.compiler.model.EnumType;
import org.shypl.biser.compiler.model.Model;
import org.shypl.biser.compiler.model.StructureType;

public class CppModuleBuilder implements ModuleBuilder {
	@Override
	public String getLang() {
		return "cpp";
	}
	
	@Override
	public void build(Module module, Model model) throws CompilerException {
		for (StructureType type : model.getStructures()) {
			if (type instanceof EntityType) {
				buildEntity((EntityType)type);
			}
			else if (type instanceof EnumType) {
//				throw new RuntimeException("TODO")
			}
		}
	}
	
	private void buildEntity(EntityType type) {
	
	
	}
}
