package org.shypl.biser.compiler.parser;

import org.shypl.biser.compiler.Utils;
import org.shypl.biser.compiler.model.Api;
import org.shypl.biser.compiler.model.ApiServiceMethod;
import org.shypl.biser.compiler.model.ApiService;
import org.shypl.biser.compiler.model.ApiSide;
import org.shypl.biser.compiler.model.ArrayType;
import org.shypl.biser.compiler.model.DataType;
import org.shypl.biser.compiler.model.EntityType;
import org.shypl.biser.compiler.model.EnumType;
import org.shypl.biser.compiler.model.MapType;
import org.shypl.biser.compiler.model.Model;
import org.shypl.biser.compiler.model.ModelBuilder;
import org.shypl.biser.compiler.model.ModelException;
import org.shypl.biser.compiler.model.Parameter;
import org.shypl.biser.compiler.model.PrimitiveType;
import org.shypl.biser.compiler.model.StructureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Parser {

	private final ModelBuilder modelBuilder;
	private Map<String, StructureTypeProxy> structureProxies = new HashMap<>();

	public Parser() {
		this(new ModelBuilder());
	}

	public Parser(ModelBuilder modelBuilder) {
		this.modelBuilder = modelBuilder;
	}

	public Model buildModel() throws ParserException {
		for (StructureTypeProxy proxy : structureProxies.values()) {
			final StructureType structure = modelBuilder.getStructure(proxy.getName());
			if (structure == null) {
				throw new ParserException("Structure named " + proxy.getName() + " not defined");
			}
			proxy.setSource(structure);
		}
		structureProxies.clear();

		return modelBuilder.buildModel();
	}

	public void parse(TokenStream stream) throws ParserException {
		while (stream.hasNext()) {
			TokenEntry entry = stream.next();

			if (entry.isToken(Token.WORD)) {
				parseStructure(stream, entry.getValue());
			}
			else if (entry.isToken(Token.CARET)) {
				parseApiGate(stream);
			}
			else {
				throw new UnexpectedTokenException(entry);
			}
		}
	}

	private void parseStructure(TokenStream stream, String name) throws ParserException {
		StructureType type;
		TokenEntry entry = stream.next();

		if (entry.isToken(Token.BRACKET_SQUARE_OPEN)) {
			parseEnum(stream, name);
		}
		else {
			String parentName;

			if (entry.isToken(Token.WORD)) {
				parentName = entry.getValue();
				entry = stream.next();
			}
			else {
				parentName = null;
			}

			if (!entry.isToken(Token.BRACKET_CURLY_OPEN)) {
				throw new UnexpectedTokenException(entry);
			}

			parseEntity(stream, name, parentName);
		}
	}

	private EnumType parseEnum(TokenStream stream, String name) throws ParserException {
		EnumType type;
		try {
			type = modelBuilder.getEnum(name);
		}
		catch (ModelException e) {
			throw new ParserException("Error on parse enum " + name, e);
		}

		while (true) {
			TokenEntry entry = stream.next();
			if (entry.isToken(Token.BRACKET_SQUARE_CLOSE)) {
				break;
			}
			if (entry.isToken(Token.WORD)) {
				type.addValue(entry.getValue());
				stream.skip(Token.COMMA);
			}
			else {
				throw new UnexpectedTokenException(entry);
			}
		}

		return type;
	}

	private EntityType parseEntity(TokenStream stream, String name, String parentName) throws ParserException {
		EntityType type;
		try {
			type = modelBuilder.getEntity(name);
			if (parentName != null) {
				type.setParent(modelBuilder.getEntity(parentName));
			}
		}
		catch (ModelException e) {
			throw new ParserException("Error on parse entity " + name, e);
		}


		for (Parameter parameter : parseParameters(stream, name)) {
			type.addField(parameter);
		}

		stream.next(Token.BRACKET_CURLY_CLOSE);

		return type;
	}

	private DataType parseType(final TokenStream stream, String inlineStructureName) throws ParserException {
		final TokenEntry entry = stream.next();

		switch (entry.getToken()) {
			case WORD: {
				final String name = entry.getValue();
				if (stream.skip(Token.BRACKET_CURLY_OPEN)) {
					return parseEntity(stream, inlineStructureName, name);
				}
				return provideType(name);
			}
			case BRACKET_SQUARE_OPEN:
				return parseEnum(stream, inlineStructureName);

			case BRACKET_CURLY_OPEN:
				return parseEntity(stream, inlineStructureName, null);

			case STAR:
				return new ArrayType(parseType(stream, Utils.convertToSingular(inlineStructureName)));

			case BRACKET_ANGLE_OPEN: {
				inlineStructureName = Utils.convertToSingular(inlineStructureName);
				final DataType keyType = parseType(stream, inlineStructureName + "Key");
				stream.next(Token.MINUS);
				final DataType valueType = parseType(stream, inlineStructureName + "Value");
				stream.next(Token.BRACKET_ANGLE_CLOSE);
				return new MapType(keyType, valueType);
			}

			default:
				throw new UnexpectedTokenException(entry);
		}
	}

	private DataType provideType(String name) {
		DataType type = PrimitiveType.get(name);

		if (type == null) {
			type = modelBuilder.getStructure(name);
			if (type == null) {
				type = structureProxies.get(name);
				if (type == null) {
					StructureTypeProxy proxy = new StructureTypeProxy(name);
					structureProxies.put(name, proxy);
					type = proxy;
				}
			}
		}

		return type;
	}

	private Collection<Parameter> parseParameters(TokenStream stream, final String ownerName) throws ParserException {
		final Collection<Parameter> parameters = new ArrayList<>();

		while (true) {
			TokenEntry entry = stream.next();
			if (entry.isToken(Token.WORD)) {
				final String name = entry.getValue();
				stream.next(Token.COLON);
				parameters.add(new Parameter(name, parseType(stream, ownerName + Utils.convertToCamel(name))));
				stream.skip(Token.COMMA);
			}
			else {
				stream.back();
				break;
			}
		}

		return parameters;
	}

	private void parseApiGate(TokenStream stream) throws ParserException {
		Api gate = modelBuilder.getCsiGate(stream.next(Token.WORD).getValue());
		TokenEntry entry = stream.next();

		if (entry.isToken(Token.DOT)) {
			parseApiService(stream, gate, stream.next(Token.WORD).getValue());
		}
		else if (entry.isToken(Token.BRACKET_CURLY_OPEN)) {
			while (true) {
				entry = stream.next();
				if (entry.isToken(Token.BRACKET_CURLY_CLOSE)) {
					break;
				}
				if (entry.isToken(Token.WORD)) {
					parseApiService(stream, gate, entry.getValue());
				}
				else {
					throw new UnexpectedTokenException(entry);
				}
			}
		}
		else {
			throw new UnexpectedTokenException(entry);
		}
	}

	private void parseApiService(TokenStream stream, Api api, String name) throws ParserException {
		ApiService service = api.getService(name);

		stream.next(Token.BRACKET_CURLY_OPEN);

		String namePrefix = api.getName() + '.' + service.getCamelName();

		while (true) {
			final TokenEntry entry = stream.next();
			if (entry.isToken(Token.BRACKET_CURLY_CLOSE)) {
				break;
			}
			try {
				if (entry.isToken(Token.BRACKET_ANGLE_CLOSE)) {
					String actionName = stream.next(Token.WORD).getValue();
					service.addAction(parseApiAction(stream, actionName, ApiSide.SERVER, false, namePrefix + Utils.convertToCamel(actionName)));
				}
				else if (entry.isToken(Token.BRACKET_ANGLE_OPEN)) {
					boolean global = stream.skip(Token.BRACKET_ANGLE_OPEN);
					String actionName = stream.next(Token.WORD).getValue();
					service.addAction(parseApiAction(stream, actionName, ApiSide.CLIENT, global, namePrefix + Utils.convertToCamel(actionName)));
				}
				else {
					throw new UnexpectedTokenException(entry);
				}
			}
			catch (ModelException e) {
				throw new ParserException("Error on parse api service " + api.getName() + "." + name, e);
			}
		}
	}

	private ApiServiceMethod parseApiAction(TokenStream stream, String name, ApiSide side, boolean global, String namePrefix) throws ParserException {
		final ApiServiceMethod action = new ApiServiceMethod(name, side, global);

		if (stream.skip(Token.BRACKET_ROUND_OPEN)) {
			for (Parameter parameter : parseParameters(stream, namePrefix)) {
				try {
					action.addArgument(parameter);
				}
				catch (ModelException e) {
					throw new ParserException("Error on parse api service " + name, e);
				}
			}
			stream.next(Token.BRACKET_ROUND_CLOSE);
		}

		if (stream.skip(Token.COLON)) {

			if (stream.skip(Token.COLON)) {
				action.setResultDeferred(true);
			}
			action.setResultType(parseType(stream, namePrefix + "Result"));
		}

		return action;
	}
}
