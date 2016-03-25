package org.shypl.biser.compiler.model;

import org.shypl.biser.compiler.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiServiceMethod {
	private final Map<String, Parameter> arguments = new LinkedHashMap<>();
	private final String   name;
	private final ApiSide  side;
	private final boolean  global;
	private final String   camelName;
	private       boolean  resultDeferred;
	private       DataType resultType;
	private       int      id;

	public ApiServiceMethod(String name, ApiSide side, boolean global) {
		this.name = name;
		this.side = side;
		this.global = global;
		camelName = Utils.convertToCamel(name);
	}

	public int getId() {
		return id;
	}

	void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getCamelName() {
		return camelName;
	}

	public ApiSide getSide() {
		return side;
	}

	public boolean isGlobal() {
		return global;
	}

	public void addArgument(final Parameter argument) throws ModelException {
		if (arguments.containsKey(argument.getName())) {
			throw new ModelException("Argument " + argument.getName() + " already exists in action " + getName());
		}
		arguments.put(argument.getName(), argument);
	}

	public Collection<Parameter> getArguments() {
		return new ArrayList<>(arguments.values());
	}

	public boolean hasArguments() {
		return !arguments.isEmpty();
	}

	public boolean hasArgumentName(String name) {
		return arguments.containsKey(name);
	}

	public int getArgumentsSize() {
		return arguments.size();
	}

	public boolean isResultDeferred() {
		return resultDeferred;
	}

	public void setResultDeferred(boolean value) {
		resultDeferred = value;
	}

	public DataType getResultType() {
		return resultType;
	}

	public void setResultType(DataType type) {
		resultType = type;
	}

	public boolean hasResult() {
		return resultType != null;
	}
}
