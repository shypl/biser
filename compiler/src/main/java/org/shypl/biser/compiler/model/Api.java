package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Api {
	private final String name;
	private final Map<String, ApiService> services = new HashMap<>();

	public Api(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ApiService getService(String name) {
		ApiService service = services.get(name);
		if (service == null) {
			service = new ApiService(services.size() + 1, name);
			services.put(name, service);
		}
		return service;
	}

	public Collection<ApiService> getServices() {
		return new ArrayList<>(services.values());
	}

	public List<ApiService> getServerServices() {
		List<ApiService> services = new ArrayList<>();
		for (ApiService service : this.services.values()) {
			if (service.hasServerActions()) {
				services.add(service);
			}
		}
		return services;
	}

	public List<ApiService> getClientServices() {
		List<ApiService> services = new ArrayList<>();
		for (ApiService service : this.services.values()) {
			if (service.hasClientActions()) {
				services.add(service);
			}
		}
		return services;
	}
}
