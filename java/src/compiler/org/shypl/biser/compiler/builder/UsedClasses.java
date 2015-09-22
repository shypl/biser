package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodePackage;
import org.shypl.biser.compiler.code.CodeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UsedClasses {
	private final Map<CodeClass, UseInfo> classes = new HashMap<>();
	private final boolean notImportDuplicates;
	private final Collector collector = new Collector();
	private final CodePackage pack;

	public UsedClasses(CodePackage pack, boolean importDuplicates) {
		this.pack = pack;
		this.notImportDuplicates = !importDuplicates;
	}

	public void use(CodeClass cls) {
		collector.visitClass(cls);
	}

	public String getDeclaration(CodeClass type) {
		return classes.get(type).declarationFullName ? type.getFullName() : type.getName();
	}

	public Collection<CodeClass> getImportedClasses() {
		Collection<CodeClass> collection = new ArrayList<>();

		for (Map.Entry<CodeClass, UseInfo> entry : classes.entrySet()) {
			if (entry.getValue().requireImport) {
				collection.add(entry.getKey());
			}
		}

		return collection;
	}

	public void ignoreImportPackage(CodePackage pack) {
		for (Map.Entry<CodeClass, UseInfo> entry : classes.entrySet()) {
			if (entry.getKey().getPackage() == pack) {
				entry.getValue().requireImport = false;
			}
		}
	}

	private static class UseInfo {
		public boolean requireImport;
		public boolean declarationFullName;

		public UseInfo(boolean requireImport, boolean declarationFullName) {
			this.requireImport = requireImport;
			this.declarationFullName = declarationFullName;
		}
	}

	private class Collector implements CodeVisitor {
		@Override
		public void visitTypeClass(CodeClass type) {
			if (!classes.containsKey(type)) {

				boolean requireImport = type.hasPackage() && !type.getPackage().isRoot() && pack != type.getPackage();
				boolean declarationFullName = false;

				for (CodeClass cls : classes.keySet()) {
					if (cls.getName().equals(type.getName())) {
						declarationFullName = true;
						if (requireImport && notImportDuplicates) {
							requireImport = false;
						}
						break;
					}
				}

				classes.put(type, new UseInfo(requireImport, declarationFullName));
			}
		}
	}
}
