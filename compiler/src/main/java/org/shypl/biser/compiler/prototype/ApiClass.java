package org.shypl.biser.compiler.prototype;

import java.util.LinkedHashSet;
import java.util.Set;

public class ApiClass extends Class
{
	public final Set<Method> serviceMethods  = new LinkedHashSet<>();
	public final Set<Method> notifierMethods = new LinkedHashSet<>();

	public ApiClass(Package pkg, String name)
	{
		super(pkg, name);
	}

	public void addMethod(Method method)
	{
		if (method.isService) {
			serviceMethods.add(method);
		}
		else {
			notifierMethods.add(method);
		}
	}

	public boolean isService()
	{
		return !serviceMethods.isEmpty();
	}

	public boolean isNotifier()
	{
		return !notifierMethods.isEmpty();
	}
}
