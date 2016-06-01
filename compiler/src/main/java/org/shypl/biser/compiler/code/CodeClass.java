package org.shypl.biser.compiler.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CodeClass extends CodeType implements CodeModifiable {
	private final CodePackage pack;
	private final CodeModifier                      modifier         = new CodeModifier();
	private final Set<CodeClass>                    implementClasses = new HashSet<>();
	private final Set<String>                       enumValues       = new LinkedHashSet<>();
	private final CodeNamedObjectSet<CodeParameter> fields           = new CodeNamedObjectSet<>();
	private final CodeNamedObjectSet<CodeGeneric>   generics         = new CodeNamedObjectSet<>();
	private final Collection<CodeMethod>            methods          = new LinkedList<>();
	private final CodeNamedObjectSet<CodeClass>     classes          = new CodeNamedObjectSet<>();
	private CodeType parent;
	private boolean  anInterface;

	CodeClass(String name, CodePackage pack) {
		super(name);
		this.pack = pack;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		visitor.visitTypeClass(this);
	}

	@Override
	public CodeModifier getModifier() {
		return modifier;
	}

	public CodePackage getPackage() {
		return pack;
	}

	public boolean hasPackage() {
		return pack != null;
	}

	public CodeEngine getEngine() {
		return pack.getEngine();
	}

	public String getFullName() {
		CodeEngine engine = getEngine();
		return getFullName(engine.getPackagePathSeparator());
	}

	public String getFullName(char separator) {
		String path = pack.getFullName(separator);
		return path.isEmpty() ? getName() : path + separator + getName();
	}

	public boolean isEnum() {
		return !enumValues.isEmpty();
	}

	public boolean isInterface() {
		return anInterface;
	}

	public void setInterface(boolean anInterface) {
		this.anInterface = anInterface;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public CodeType getParent() {
		return parent;
	}

	public void setParent(CodeType parent) {
		this.parent = parent;
	}

	public void addImplement(CodeClass cls) {
		implementClasses.add(cls);
	}

	public boolean hasImplements() {
		return !implementClasses.isEmpty();
	}

	public List<CodeClass> getImplements() {
		return new ArrayList<>(implementClasses);
	}

	public void addEnumValue(String value) {
		enumValues.add(value);
	}

	public void addEnumValues(Collection<String> values) {
		enumValues.addAll(values);
	}

	public Set<String> getEnumValues() {
		return new LinkedHashSet<>(enumValues);
	}

	public CodeParameter getField(String name) {
		CodeParameter field = fields.get(name);
		if (field == null) {
			field = fields.add(new CodeParameter(name));
		}
		return field;
	}

	public List<CodeParameter> getFields() {
		List<CodeParameter> list = fields.getElements();
		list.sort(CodeMethod.COMPARATOR);
		return list;
	}

	public boolean hasFields() {
		return !fields.isEmpty();
	}

	public CodeMethod addMethod(String name) {
		CodeMethod method = new CodeMethod(name);
		methods.add(method);
		return method;
	}

	public List<CodeMethod> getMethods() {
		List<CodeMethod> list = new ArrayList<>(this.methods);
		list.sort(new Comparator<CodeMethod>() {
			@Override
			public int compare(CodeMethod o1, CodeMethod o2) {
				boolean c1 = o1.getName().equals(getName());
				boolean c2 = o2.getName().equals(getName());
				if (c1 == c2) {
					return CodeMethod.COMPARATOR.compare(o1, o2);
				}
				return c1 ? -1 : 1;
			}
		});
		return list;
	}

	public boolean hasMethods() {
		return !methods.isEmpty();
	}

	public CodeParametrizedClass parametrize(CodeType... parameters) {
		return new CodeParametrizedClass(this, parameters);
	}

	public CodeGeneric getGeneric(String name) {
		CodeGeneric generic = generics.get(name);
		if (generic == null) {
			generic = generics.add(new CodeGeneric(name));
		}
		return generic;
	}

	public List<CodeGeneric> getGenerics() {
		return generics.getElements();
	}

	public boolean hasGenerics() {
		return !generics.isEmpty();
	}

	public CodeClass getClass(String name) {
		CodeClass cls = classes.get(name);
		if (cls == null) {
			cls = classes.add(new CodeClass(name, null));
		}
		return cls;
	}

	public List<CodeClass> getClasses() {
		return classes.getElements();
	}

	public boolean hasClasses() {
		return !classes.isEmpty();
	}
}
