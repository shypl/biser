package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.code.CodeClass;
import org.shypl.biser.compiler.code.CodePackage;
import org.shypl.biser.compiler.code.CodeVisitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

	public List<CodeClass> getImportedClasses() {
		return getImportedClasses(null);
	}

	public List<CodeClass> getImportedClasses(Comparator<CodeClass> comparator) {
		List<CodeClass> list = new ArrayList<>();

		for (Map.Entry<CodeClass, UseInfo> entry : classes.entrySet()) {
			if (entry.getValue().requireImport) {
				list.add(entry.getKey());
			}
		}

		if (comparator == null) {
			list.sort((o1, o2) -> o1.getFullName().compareTo(o2.getFullName()));
		}
		else {
			list.sort((o1, o2) -> {
				int compare = comparator.compare(o1, o2);
				if (compare == 0) {
					return o1.getFullName().compareTo(o2.getFullName());
				}
				return compare;
			});
		}

		return list;
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
