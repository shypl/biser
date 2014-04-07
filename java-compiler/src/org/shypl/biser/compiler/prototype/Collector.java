package org.shypl.biser.compiler.prototype;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Collector extends EntityContainer
{
	private boolean linkingRequired;
	private Map<String, Service> services = new HashMap<>();

	public Collector()
	{
		super(null, "<Collector>");
	}

	public void add(final Reader reader) throws IOException, PrototypeException
	{
		linkingRequired = true;
		new Parser(new Tokenizer(reader)).parse(this);
	}

	public Service[] getServices() throws PrototypeException
	{
		linking();
		return services.values().toArray(new Service[services.size()]);
	}

	int addService(final Service service) throws DuplicateNameException
	{
		if (services.containsKey(service.name)) {
			throw new DuplicateNameException("Duplicate service", service.name);
		}
		services.put(service.name, service);
		return services.size();
	}

	@Override
	void linking() throws PrototypeException
	{
		if (linkingRequired) {
			linkingRequired = false;
			super.linking();
			for (Service service : services.values()) {
				service.linking();
			}
		}
	}
}
