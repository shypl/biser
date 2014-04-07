package org.shypl.biser.compiler.prototype;

import org.shypl.biser.compiler.Side;
import org.shypl.biser.compiler.Utils;

public class Parser
{
	static private String normalizeName(String name, boolean toSingular)
	{
		return Utils.toCamelCase(toSingular ? Utils.toSingular(name) : name);
	}

	private final Tokenizer tokenizer;

	Parser(final Tokenizer tokenizer)
	{
		this.tokenizer = tokenizer;
	}

	public void parse(final Collector collector) throws PrototypeException
	{
		while (tokenizer.hasNext()) {
			switch (tokenizer.next()) {
				case WORD:
					parseEntity(collector);
					break;
				case CARET:
					parseService(collector);
					break;
				default:
					throw tokenizer.createUnexpectedTokenException();
			}
		}
	}

	private void parseEntity(final EntityContainer container) throws PrototypeException
	{
		final String name = tokenizer.word();
		Token token = tokenizer.next();

		if (token == Token.BRACKET_SQUARE_OPEN) {
			parseEntityEnum(new Entity(container, name, true));
		}
		else {
			String parentName = null;

			if (token == Token.WORD) {
				parentName = tokenizer.word();
				tokenizer.next();
			}

			parseEntityObject(new Entity(container, name, parentName));
		}
	}

	private void parseEntityContainer(final EntityContainer container, final String name) throws PrototypeException
	{
		switch (tokenizer.next()) {
			case BRACKET_SQUARE_OPEN:
				parseEntityEnum(new Entity(container, name, true));
				break;

			case BRACKET_CURLY_OPEN:
				parseEntityObject(new Entity(container, name));
				break;

			case WORD: {
				final String parentName = tokenizer.word();
				tokenizer.next(Token.BRACKET_CURLY_OPEN);
				parseEntityObject(new Entity(container, name, parentName));
				break;
			}

			default:
				throw tokenizer.createUnexpectedTokenException();
		}
	}

	private Entity parseEntityEnum(final Entity entity) throws PrototypeException
	{
		try {
			tokenizer.check(Token.BRACKET_SQUARE_OPEN);
			while (true) {
				Token token = tokenizer.next();
				if (token == Token.BRACKET_SQUARE_CLOSE) {
					break;
				}
				tokenizer.check(Token.WORD);
				entity.addEnumValue(tokenizer.word());
			}
			return entity;
		}
		catch (DuplicateNameException e) {
			throw e.expandName(entity.name);
		}
	}

	private Entity parseEntityObject(final Entity entity) throws PrototypeException
	{
		tokenizer.check(Token.BRACKET_CURLY_OPEN);
		try {
			while (true) {
				if (tokenizer.next() == Token.BRACKET_CURLY_CLOSE) {
					break;
				}
				tokenizer.check(Token.WORD);
				final String name = tokenizer.word();
				if (tokenizer.next() == Token.COLON) {
					entity.addProperty(new Parameter(name, parseType(name, entity, false)));
				}
				else {
					tokenizer.prev();
					parseEntityContainer(entity, name);
				}
			}
		}
		catch (DuplicateNameException e) {
			throw e.expandName(entity.name);
		}
		return entity;
	}

	private Parameter parseParameter(String namePrefix, final EntityContainer container) throws PrototypeException
	{
		if (tokenizer.next() == Token.BRACKET_ROUND_CLOSE) {
			return null;
		}
		tokenizer.check(Token.WORD);
		String name = tokenizer.word();
		tokenizer.next(Token.COLON);
		return new Parameter(name, parseType(namePrefix + name, container, false));
	}

	private void parseService(final Collector collector) throws PrototypeException
	{
		tokenizer.next(Token.WORD);
		final Service service = new Service(collector, tokenizer.word());
		tokenizer.next(Token.BRACKET_CURLY_OPEN);

		try {
			while (true) {
				Token token = tokenizer.next();
				if (token == Token.BRACKET_CURLY_CLOSE) {
					break;
				}

				if (token == Token.WORD) {
					parseEntityContainer(service, tokenizer.word());
					continue;
				}

				switch (token) {
					case BRACKET_ANGLE_OPEN:
						parseServiceMethod(service, Side.CLIENT);
						break;
					case BRACKET_ANGLE_CLOSE:
						parseServiceMethod(service, Side.SERVER);
						break;
					default:
						throw tokenizer.createUnexpectedTokenException();
				}
			}
		}
		catch (DuplicateNameException e) {
			throw e.expandName(service.name);
		}
	}

	private void parseServiceMethod(final Service service, final Side side) throws PrototypeException
	{
		tokenizer.next(Token.WORD);

		Parameter parameter;
		final ServiceMethod method = new ServiceMethod(service, side, tokenizer.word());

		Token token = tokenizer.next();

		try {
			if (token == Token.BRACKET_ROUND_OPEN) {
				while (null != (parameter = parseParameter(method.name + "_", service))) {
					method.addArgument(parameter);
				}
				token = tokenizer.next();
			}

			if (token == Token.COLON) {
				if (side == Side.CLIENT) {
					throw tokenizer.createUnexpectedTokenException();
				}

				if (tokenizer.next() == Token.BRACKET_ROUND_OPEN) {
					token = tokenizer.next();
					if (token == Token.BRACKET_ROUND_CLOSE) {
						throw tokenizer.createUnexpectedTokenException();
					}

					boolean multi = false;
					if (token == Token.WORD) {
						if (tokenizer.next() == Token.COLON) {
							multi = true;
						}
						tokenizer.prev();
					}
					tokenizer.prev();

					if (multi) {
						final ServiceMethodResult.LazyMulti result = new ServiceMethodResult.LazyMulti();
						method.setResult(result);
						while (null != (parameter = parseParameter(method.name + "Result_", service))) {
							result.addParameter(parameter);
						}
					}
					else {
						method
							.setResult(new ServiceMethodResult.Lazy(parseType(method.name + "Result", service, false)));
						tokenizer.next(Token.BRACKET_ROUND_CLOSE);
					}
				}
				else {
					tokenizer.prev();
					method.setResult(new ServiceMethodResult.Simple(parseType(method.name + "Result", service, false)));
				}
			}
			else {
				tokenizer.prev();
			}
		}
		catch (DuplicateNameException e) {
			throw e.expandName(method.name);
		}
	}

	private Type parseType(String parameterName, final EntityContainer container, final boolean isCollection)
		throws PrototypeException
	{
		Token token = tokenizer.next();

		switch (token) {
			case WORD: {
				final String name = tokenizer.word();
				final Type.Primitive primitive = Type.Primitive.define(name);

				if (primitive != null) {
					return primitive;
				}

				if (tokenizer.next() == Token.BRACKET_CURLY_OPEN) {
					return new Type.Entity(
						parseEntityObject(new Entity(container, normalizeName(parameterName, isCollection), name)));
				}

				tokenizer.prev();
				return new Type.Entity(name, container);
			}

			case BRACKET_CURLY_OPEN:
				return new Type.Entity(
					parseEntityObject(new Entity(container, normalizeName(parameterName, isCollection))));

			case BRACKET_SQUARE_OPEN:
				return new Type.Entity(
					parseEntityEnum(new Entity(container, normalizeName(parameterName, isCollection), true)));

			case STAR:
				return new Type.List(parseType(parameterName, container, true), true);

			case DOLLAR:
				return new Type.List(parseType(parameterName, container, true), false);

			case AMPERSAND: {
				if (isCollection) {
					parameterName = Utils.toSingular(parameterName);
				}
				final Type key = parseType(parameterName + "Key", container, false);
				tokenizer.next(Token.MINUS);
				return new Type.Map(key, parseType(parameterName + "Value", container, false));
			}

			default:
				throw tokenizer.createUnexpectedTokenException();
		}
	}
}
