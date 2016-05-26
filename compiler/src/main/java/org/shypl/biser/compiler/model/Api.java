package org.shypl.biser.compiler.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Api {
	private final Map<String, ApiService> services = new HashMap<>();

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
		return this.services.values().stream().filter(ApiService::hasServerActions).collect(Collectors.toList());
	}

	public List<ApiService> getClientServices() {
		return this.services.values().stream().filter(ApiService::hasClientActions).collect(Collectors.toList());
	}
}
