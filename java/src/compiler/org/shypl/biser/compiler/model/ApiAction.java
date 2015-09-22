package org.shypl.biser.compiler.model;

import org.shypl.biser.compiler.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiAction {
	private final Map<String, Parameter> arguments = new LinkedHashMap<>();
	private final String   name;
	private final ApiSide  side;
	private final boolean  global;
	private final String   camelName;
	private       boolean  resultDeferred;
	private       DataType resultType;
	private       int      id;

	public ApiAction(String name, ApiSide side, boolean global) {
		this.name = name;
		this.side = side;
		this.global = global;
		camelName = Utils.convertToCamel(name);
	}

	public int getId() {
		return id;
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

	public int getArgumentsSize() {
		return arguments.size();
	}

	public void setResultDeferred(boolean value) {
		resultDeferred = value;
	}

	public void setResultType(DataType type) {
		resultType = type;
	}

	public boolean isResultDeferred() {
		return resultDeferred;
	}

	public DataType getResultType() {
		return resultType;
	}

	public boolean hasResult() {
		return resultType != null;
	}

	void setId(int id) {
		this.id = id;
	}
}
