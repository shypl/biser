package org.shypl.biser.compiler.code;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CodeClass extends CodeType implements CodeModifiable {
	public static final Comparator<CodeParameter> PARAMETER_COMPARATOR = new Comparator<CodeParameter>() {
		@Override
		public int compare(CodeParameter o1, CodeParameter o2) {
			CodeModifier m1 = o1.getModifier();
			CodeModifier m2 = o2.getModifier();

			if (m1.equals(m2)) {
				return 0;
			}

			if (compare0(m1, m2)) {
				return -1;
			}

			if (compare0(m2, m1)) {
				return 1;
			}

			return 0;
		}

		private boolean compare0(CodeModifier m1, CodeModifier m2) {
			return m1.is(Modifier.STATIC) && m2.not(Modifier.STATIC)
				|| m1.is(Modifier.FINAL) && m2.not(Modifier.FINAL);
		}
	};

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

	public void setParent(CodeType parent) {
		this.parent = parent;
	}

	public boolean isEnum() {
		return !enumValues.isEmpty();
	}

	public boolean isInterface() {
		return anInterface;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public CodeType getParent() {
		return parent;
	}

	public void addImplement(CodeClass cls) {
		implementClasses.add(cls);
	}

	public boolean hasImplements() {
		return !implementClasses.isEmpty();
	}

	public Collection<CodeClass> getImplements() {
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

	public Collection<CodeParameter> getFields() {
		List<CodeParameter> elements = fields.getElements();
		elements.sort(PARAMETER_COMPARATOR);
		return elements;
	}

	public boolean hasFields() {
		return !fields.isEmpty();
	}

	public CodeMethod addMethod(String name) {
		CodeMethod method = new CodeMethod(name);
		methods.add(method);
		return method;
	}

	public Collection<CodeMethod> getMethods() {
		return new ArrayList<>(methods);
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

	public Collection<CodeGeneric> getGenerics() {
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

	public Collection<CodeClass> getClasses() {
		return classes.getElements();
	}

	public boolean hasClasses() {
		return !classes.isEmpty();
	}

	public void setInterface(boolean anInterface) {
		this.anInterface = anInterface;
	}
}
