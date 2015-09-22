package org.shypl.biser.compiler.model;

import org.shypl.biser.compiler.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApiService {
	private final int id;
	private final String name;
	private final Map<String, ApiAction> serverActions = new HashMap<>();
	private final Map<String, ApiAction> clientActions = new HashMap<>();
	private final String camelName;

	public ApiService(int id, String name) {
		this.id = id;
		this.name = name;
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

	public void addAction(ApiAction action) throws ModelException {
		final Map<String, ApiAction> map = action.getSide() == ApiSide.SERVER ? serverActions : clientActions;

		if (map.containsKey(action.getName())) {
			throw new ModelException("Action " + action.getName() + " already exists in " + action.getSide().name() + " service " + getName());
		}

		map.put(action.getName(), action);
		action.setId(map.size());
	}

	public boolean hasServerActions() {
		return !serverActions.isEmpty();
	}

	public boolean hasClientActions() {
		return !clientActions.isEmpty();
	}

	public Collection<ApiAction> getServerActions() {
		return serverActions.values();
	}

	public Collection<ApiAction> getClientActions() {
		return clientActions.values();
	}
}
