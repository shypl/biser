package org.shypl.biser.compiler;

import org.shypl.biser.compiler.prototype.ApiClass;
import org.shypl.biser.compiler.prototype.Class;
import org.shypl.biser.compiler.prototype.DataClass;
import org.shypl.biser.compiler.prototype.DataType;
import org.shypl.biser.compiler.prototype.EnumDataClass;
import org.shypl.biser.compiler.prototype.Method;
import org.shypl.biser.compiler.prototype.ObjectDataClass;
import org.shypl.biser.compiler.prototype.Package;
import org.shypl.biser.compiler.prototype.Property;

public class Parser
{
	static private String convertPropertyToClassName(String name)
	{
		return Utils.toCamelCase(Utils.toSingular(name), true);
	}

	private final Package pkg;
	private final Tokenizer tokenizer;

	public Parser(Package pkg, Tokenizer tokenizer) throws TokenizerException
	{
		this.tokenizer = tokenizer;

		while (true) {
			if (tokenizer.next() == Token.AT) {
				ParserProperty property = parseProperty();

				switch (property.name) {
					case "package":
						pkg = pkg.child(property.value);
						break;
				}

			}
			else  {
				tokenizer.prev();
				break;
			}
		}

		this.pkg = pkg;
	}

	private ParserProperty parseProperty() throws TokenizerException
	{
		tokenizer.next(Token.WORD);
		String name = tokenizer.word();
		tokenizer.next(Token.WORD);

		return new ParserProperty(name, tokenizer.word());
	}

	public Class parseClass() throws TokenizerException
	{
		Token token = tokenizer.next();
		if (token == Token.WORD) {
			return parseDataClass(null);
		}

		if (token == Token.CARET) {
			return parseApiClass();
		}

		throw tokenizer.createUnexpectedTokenException();
	}

	private ApiClass parseApiClass() throws TokenizerException
	{
		tokenizer.next(Token.WORD);
		ApiClass cls = new ApiClass(pkg, tokenizer.word());

		tokenizer.next(Token.CURLY_BRACKET_OPEN);

		while (true) {
			Token token = tokenizer.next();
			if (token == Token.CURLY_BRACKET_CLOSE) {
				break;
			}

			if (token == Token.WORD) {
				String name = tokenizer.word();
				token = tokenizer.next();

				switch (token) {
					case SQUARE_BRACKET_OPEN:
						parseEnumDataClassBody(new EnumDataClass(pkg, name, cls));
						break;

					case CURLY_BRACKET_OPEN:
						parseObjectDataClassBody(new ObjectDataClass(pkg, name, cls, null));
						break;

					case WORD: {
						String parentName = tokenizer.word();
						tokenizer.next(Token.CURLY_BRACKET_OPEN);
						parseObjectDataClassBody(new ObjectDataClass(pkg, name, cls, parentName));
						break;
					}

					default:
						throw tokenizer.createUnexpectedTokenException();
				}

				continue;
			}

			switch (token) {
				case ANGLE_BRACKET_OPEN:
					cls.addMethod(parseMethod(cls, false));
					break;
				case ANGLE_BRACKET_CLOSE:
					cls.addMethod(parseMethod(cls, true));
					break;
				default:
					throw tokenizer.createUnexpectedTokenException();
			}

		}


		return cls;
	}

	private Method parseMethod(ApiClass cls, boolean isService) throws TokenizerException
	{
		tokenizer.next(Token.WORD);
		Method method = new Method(tokenizer.word(), isService);

		Token token = tokenizer.next();

		if (token == Token.ROUND_BRACKET_OPEN) {
			while (true) {
				token = tokenizer.next();
				if (token == Token.ROUND_BRACKET_CLOSE) {
					break;
				}
				tokenizer.check(Token.WORD);
				String name = tokenizer.word();
				tokenizer.next(Token.COLON);
				method.addProperty(new Property(name, parseDataType(method.name + "_" + name, cls)));
			}
			token = tokenizer.next();
		}

		if (token == Token.COLON) {
			if (!isService) {
				throw tokenizer.createUnexpectedTokenException();
			}
			method.serResult(parseDataType(method.name + "Result", cls));
		}
		else {
			tokenizer.prev();
		}

		return method;
	}

	private DataClass parseDataClass(Class scope) throws TokenizerException
	{
		String name = tokenizer.word();
		Token token = tokenizer.next();

		if (token == Token.ANGLE_BRACKET_OPEN) {
			EnumDataClass cls = new EnumDataClass(pkg, name, scope);
			parseEnumDataClassBody(cls);
			return cls;
		}

		String parentName = null;

		if (token == Token.WORD) {
			parentName = tokenizer.word();
			tokenizer.next();
		}

		ObjectDataClass cls = new ObjectDataClass(pkg, name, scope, parentName);

		parseObjectDataClassBody(cls);

		return cls;
	}

	private void parseObjectDataClassBody(ObjectDataClass cls) throws TokenizerException
	{
		tokenizer.check(Token.CURLY_BRACKET_OPEN);

		while (true) {
			Token token = tokenizer.next();
			if (token == Token.CURLY_BRACKET_CLOSE) {
				break;
			}
			tokenizer.check(Token.WORD);

			String name = tokenizer.word();
			token = tokenizer.next();

			switch (token) {
				case COLON:
					cls.addProperty(new Property(name, parseDataType(name, cls)));
					break;

				case SQUARE_BRACKET_OPEN:
					parseEnumDataClassBody(new EnumDataClass(pkg, name, cls));
					break;

				case CURLY_BRACKET_OPEN:
					parseObjectDataClassBody(new ObjectDataClass(pkg, name, cls, null));
					break;

				case WORD: {
					String parentName = tokenizer.word();
					tokenizer.next(Token.CURLY_BRACKET_OPEN);
					parseObjectDataClassBody(new ObjectDataClass(pkg, name, cls, parentName));
					break;
				}

				default:
					throw tokenizer.createUnexpectedTokenException();
			}
		}
	}

	private void parseEnumDataClassBody(EnumDataClass cls) throws TokenizerException
	{
		tokenizer.check(Token.SQUARE_BRACKET_OPEN);
		while (true) {
			Token token = tokenizer.next();
			if (token == Token.SQUARE_BRACKET_CLOSE) {
				break;
			}
			tokenizer.check(Token.WORD);
			cls.addValue(tokenizer.word());
		}
	}

	private DataType parseDataType(String propertyName, Class scope) throws TokenizerException
	{
		Token token = tokenizer.next();

		switch (token) {
			case WORD: {
				String type = tokenizer.word();

				switch (type) {
					case "bool":
						return DataType.Primitive.BOOL;
					case "byte":
						return DataType.Primitive.BYTE;
					case "short":
						return DataType.Primitive.SHORT;
					case "int":
						return DataType.Primitive.INT;
					case "uint":
						return DataType.Primitive.UINT;
					case "double":
						return DataType.Primitive.DOUBLE;
					case "string":
						return DataType.Primitive.STRING;
					case "bytes":
						return DataType.Primitive.BYTES;
				}

				if (tokenizer.next() == Token.CURLY_BRACKET_OPEN) {
					ObjectDataClass cls = new ObjectDataClass(pkg, convertPropertyToClassName(propertyName), scope, type);
					parseObjectDataClassBody(cls);
					return new DataType.Data(cls);
				}
				tokenizer.prev();
				return new DataType.Data(type, scope);
			}

			case CURLY_BRACKET_OPEN: {
				ObjectDataClass cls = new ObjectDataClass(pkg, convertPropertyToClassName(propertyName), scope, null);
				parseObjectDataClassBody(cls);
				return new DataType.Data(cls);
			}

			case SQUARE_BRACKET_OPEN: {
				EnumDataClass cls = new EnumDataClass(pkg, convertPropertyToClassName(propertyName), scope);
				parseEnumDataClassBody(cls);
				return new DataType.Data(cls);
			}

			case STAR:
				return new DataType.Array(parseDataType(propertyName, scope));

			case DOLLAR:
				return new DataType.List(parseDataType(propertyName, scope));

			case HASH: {
				final DataType key = parseDataType(propertyName + "Key", scope);
				tokenizer.next(Token.MINUS);
				return new DataType.Map(key, parseDataType(propertyName + "Value", scope));
			}

			default:
				throw tokenizer.createUnexpectedTokenException();
		}

	}
}
