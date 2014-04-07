package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.Side;
import org.shypl.biser.compiler.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class Service extends EntityContainer
{
	public final int id;
	private final Map<String, ServiceMethod> serverMethods = new LinkedHashMap<>();
	private final Map<String, ServiceMethod> clientMethods = new LinkedHashMap<>();
	private final String normalName;

	Service(final Collector collector, final String name) throws DuplicateNameException
	{
		super(collector, name);
		this.id = collector.addService(this);

		normalName = Utils.toCamelCase(name) + "Service";
	}

	public ServiceMethod[] getClientMethods()
	{
		return clientMethods.values().toArray(new ServiceMethod[clientMethods.size()]);
	}

	@Override
	public String getFullName()
	{
		return normalName;
	}

	public ServiceMethod[] getServerMethods()
	{
		return serverMethods.values().toArray(new ServiceMethod[serverMethods.size()]);
	}

	public boolean hasClientMethods()
	{
		return !clientMethods.isEmpty();
	}

	public boolean hasServerMethods()
	{
		return !serverMethods.isEmpty();
	}

	int addMethod(final ServiceMethod method) throws DuplicateNameException
	{
		final Map<String, ServiceMethod> list = method.side == Side.CLIENT ? clientMethods : serverMethods;
		if (list.containsKey(method.name)) {
			throw new DuplicateNameException("Duplicate service method", method.name);
		}
		list.put(method.name, method);

		return list.size();
	}

	@Override
	void linking() throws PrototypeException
	{
		super.linking();
		for (ServiceMethod method : serverMethods.values()) {
			method.linking();
		}
	}
}
