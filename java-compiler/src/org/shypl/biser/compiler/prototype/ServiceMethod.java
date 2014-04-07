package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.Side;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceMethod extends Name
{
	public final Side side;
	public final int  id;
	private final Map<String, Parameter> arguments = new LinkedHashMap<>();
	private ServiceMethodResult result;

	ServiceMethod(final Service service, final Side side, final String name) throws DuplicateNameException
	{
		super(name);
		this.side = side;
		this.id = service.addMethod(this);
	}

	public Parameter[] getArguments()
	{
		return arguments.values().toArray(new Parameter[arguments.size()]);
	}

	public ServiceMethodResult getResult()
	{
		return result;
	}

	public boolean hasArguments()
	{
		return !arguments.isEmpty();
	}

	public boolean hasResult()
	{
		return result != null;
	}

	void addArgument(final Parameter argument) throws DuplicateNameException
	{
		if (arguments.containsKey(argument.name)) {
			throw new DuplicateNameException("Duplicate service method argument", argument.name);
		}
		arguments.put(argument.name, argument);
	}

	void linking() throws PrototypeException
	{
		for (Parameter parameter : arguments.values()) {
			parameter.type.linking();
		}

		if (result != null) {
			result.linking();
		}
	}

	void setResult(final ServiceMethodResult result)
	{
		this.result = result;
	}
}
