package org.shypl.biser.compiler.builder;

import org.shypl.biser.compiler.Side;
import org.shypl.biser.compiler.prototype.Entity;
import org.shypl.biser.compiler.prototype.Parameter;
import org.shypl.biser.compiler.prototype.Service;
import org.shypl.biser.compiler.prototype.Type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Builder
{
	protected static final String VOID = "void";

	protected final Set<Type>   decodeCollections = new HashSet<>();
	protected final Set<Type>   encodeCollections = new HashSet<>();
	protected final Set<Entity> decodeFactories   = new HashSet<>();
	protected final Set<Entity> encodeFactories   = new HashSet<>();
	protected final Set<Entity> decodes           = new HashSet<>();
	protected final Set<Entity> encodes           = new HashSet<>();

	protected final String collectionFactory;
	protected final String pkg;
	private final Map<Object, CodeClass> classes = new HashMap<>();
	private final Path   path;
	private final String pkgApi;
	private final Side   side;
	private final String ext;

	protected Builder(final Path path, final String pkg, final Side side, final String ext, final String collectionFactory)
	{
		this.path = path;
		this.pkg = pkg;
		this.side = side;
		this.ext = ext;
		this.collectionFactory = collectionFactory;

		pkgApi = pkg + '.' + (side == Side.CLIENT ? "client" : "server");
	}

	public void build(final Entity[] entities, final Service[] services) throws IOException
	{
		if (services.length != 0) {
			if (side == Side.CLIENT) {
				buildApiClient(services);
			}
			else {
				buildApiServer(services);
			}
		}

		for (Entity entity : entities) {
			buildEntity(entity);
		}

		if (!decodeCollections.isEmpty() || !encodeCollections.isEmpty()) {
			final CodeClass cls = createClass(collectionFactory, Mod.FINAL | Mod.PUBLIC, false);
			buildCollectionFactory(cls);
			save(cls);
		}
	}

	protected CodeClass buildEntity(final Entity entity) throws IOException
	{
		final CodeClass cls = getClass(entity);

		if (entity.isEnum()) {
			cls.declareAsEnum(entity.getEnumValues());
			buildEntityBodyEnum(cls, entity);
		}
		else {
			for (Parameter property : entity.getProperties()) {
				cls.addProperty(property.name, defineType(property.type, cls), Mod.PUBLIC);
			}

			buildEntityBody(cls, entity);

			for (Entity inner : entity.getEntities()) {
				buildEntity(inner);
			}
		}

		save(cls);

		return cls;
	}

	protected CodeClass createClass(final String name, final int mod, final boolean api)
	{
		return new CodeClass(api ? pkgApi : pkg, name, mod);
	}

	protected void defineEntityDependencies(final CodeClass cls, final Entity entity)
	{
		if (entity.hasParent()) {
			cls.setParent(getClass(entity.getParent()));
		}
	}

	protected String defineType(final Type type, final CodeClass cls)
	{
		return defineType(type, cls, false);
	}

	protected CodeClass getClass(final Object object)
	{
		CodeClass cls = classes.get(object);
		if (cls == null) {
			if (object instanceof Entity) {
				cls = createEntityClass((Entity)object);
				classes.put(object, cls);
				defineEntityDependencies(cls, (Entity)object);
			}
			else if (object instanceof Service) {
				cls = createServiceClass((Service)object);
				classes.put(object, cls);
			}
		}
		return cls;
	}

	protected void save(final CodeClass cls) throws IOException
	{
		final Code code = new Code();
		buildCode(cls, code);

		Path p = path.resolve(cls.pkg.replace('.', '/'));
		p = Files.createDirectories(p);
		p = p.resolve(cls.name + '.' + ext);

		Files.write(p, code.toString().getBytes(StandardCharsets.UTF_8));
	}

	protected abstract void buildApiClient(final Service[] services) throws IOException;

	protected abstract void buildApiServer(final Service[] services) throws IOException;

	protected abstract void buildCode(final CodeClass cls, final Code code);

	protected abstract void buildCollectionFactory(final CodeClass cls);

	protected abstract void buildEntityBody(final CodeClass cls, final Entity entity);

	protected abstract void buildEntityBodyEnum(final CodeClass cls, final Entity entity);

	protected abstract String defineEntityClassName(final Entity entity);

	protected abstract String defineType(final Type type, final CodeClass cls, final boolean forCollection);

	private CodeClass createEntityClass(final Entity entity)
	{
		return createClass(defineEntityClassName(entity), Mod.PUBLIC, entity.isOwnedService());
	}

	private CodeClass createServiceClass(final Service service)
	{
		return createClass(service.getFullName(), Mod.PUBLIC | Mod.FINAL, true);
	}
}
