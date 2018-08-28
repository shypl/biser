package org.shypl.biser.compiler.builder.cpp

import org.shypl.biser.compiler.Module
import org.shypl.biser.compiler.Utils.convertToCamel
import org.shypl.biser.compiler.builder.CodeFile
import org.shypl.biser.compiler.builder.ModuleBuilder
import org.shypl.biser.compiler.model.*
import java.nio.file.Path

class CppModuleBuilder : ModuleBuilder {
	override fun getLang() = "cpp"
	
	override fun build(module: Module, model: Model) {
		
		val enumsFile = CodeFile()
		val entityFile = CodeFile()
		
		enumsFile.writeLines("#pragma once", "")
		
		entityFile.writeLines(
			"#include \"Entity.h\"",
			"#include \"DataWriter.h\"",
			"#include \"DataReader.h\"",
			"")
		
		for (type in model.structures) {
			if (type is EntityType) {
				entityFile.writeLine("#include \"${type.name}.h\"")
			}
		}
		
		entityFile.writeLines(
			"",
			"NS_BISER_IO_BEGIN",
			"",
			"Entity::Entity() : _id(-1)",
			"{",
			"}",
			"",
			"Entity::~Entity()",
			"{",
			"}",
			"",
			"Entity* Entity::create(int eId)",
			"{"
		)
			.addTab()
		
		for (type in model.structures) {
			if (type is EntityType) {
				entityFile.writeLine("if (eId == ${type.name}::ID())")
					.addTab()
					.writeLine("return ${type.name}::create();")
					.removeTab()
					.writeLine()
				
				buildEntity(module.target, type)
				
			} else if (type is EnumType) {
				buildEnum(enumsFile, type)
			}
		}
		
		entityFile
			.writeLine("return nullptr;")
			.removeTab()
			.writeLines(
				"}",
				"",
				"void Entity::setId(int value)",
				"{",
				"    _id = value;",
				"}",
				"",
				"int Entity::getId()",
				"{",
				"    return _id;",
				"}",
				"",
				"NS_BISER_IO_END"
			)
		
		enumsFile.save(module.target.resolve("csi/Objects/CsiEnums.h"))
		entityFile.save(module.target.resolve("io/Entity.cpp"))
		
		val api = model.api
		if (api.hasServices()) {
			when (module.side) {
				ApiSide.CLIENT -> buildApi(module.target, api)
				else           -> RuntimeException()
			}
		}
	}
	
	private fun buildEnum(file: CodeFile, type: EnumType) {
		file.writeLine("enum class ${type.name}")
			.writeLine("{")
			.addTab()
		
		type.values.forEachIndexed { i, s ->
			file.write(s)
			if (i == 0) {
				file.write(" = 0")
			}
			if (i == type.values.size - 1) {
				file.writeLine()
			} else {
				file.writeLine(",")
			}
		}
		
		file
			.removeTab()
			.writeLine("};")
			.writeLine()
	}
	
	private fun buildApi(dir: Path, api: Api) {
		buildApiDesc(api.services).save(dir.resolve("csi/Objects/CsiApiIds.h"))
	}
	
	private fun buildApiDesc(services: Collection<ApiService>): CodeFile {
		val file = CodeFile()
		
		file.writeLines("#pragma once", "")
			.writeLine()
		
		file.writeLine("// Services")
		services.forEach {
			file.writeLine("#define CSI_SERVICE_${it.name.toUpperUnderscoreCase()} ${it.id}")
		}
		file.writeLine()
		
		file.writeLine("// > Server actions")
		services.forEach { s ->
			if (s.hasServerActions()) {
				s.serverActions.forEach {
					file.writeLine("#define CSI_SERVER_${s.name.toUpperUnderscoreCase()}_${it.name.toUpperUnderscoreCase()} ${it.id}")
					if (it.hasResult()) {
						file.writeLine("#define CSI_SERVER_${s.name.toUpperUnderscoreCase()}_${it.name.toUpperUnderscoreCase()}_CALLBACK ${100 * s.id + it.id}")
					}
				}
				file.writeLine()
			}
		}
		
		
		file.writeLine("// < Client actions")
		services.forEach { s ->
			if (s.hasClientActions()) {
				s.clientActions.forEach {
					file.writeLine("#define CSI_CLIENT_${s.name.toUpperUnderscoreCase()}_${it.name.toUpperUnderscoreCase()} ${it.id}")
				}
				file.writeLine()
			}
		}
		
		return file
	}
	
	private fun buildEntity(dir: Path, type: EntityType) {
		buildEntityFileH(type).save(dir.resolve("csi/Objects/${type.name}.h"))
		buildEntityFileC(type).save(dir.resolve("csi/Objects/${type.name}.cpp"))
	}
	
	private fun buildEntityFileH(type: EntityType): CodeFile {
		val file = CodeFile()
		
		type.children
		
		val name = type.name
		
		file
			.writeLines(
				"#pragma once",
				"",
				"#include \"Biser.h\""
			)
		
		if (type.fields.any { it.type.isEnum }) {
			file.writeLine("#include \"CsiEnums.h\"")
		}
		
		file
			.writeLines(
				"",
				"USING_NS_CC;",
				"USING_NS_BISER_IO;",
				"using namespace std;",
				""
			)
		
		type.fields.apply {
			val representer = object : TypeRepresenter<Unit> {
				private val added = mutableSetOf<String>()
				
				override fun representPrimitive(type: PrimitiveType) {}
				
				override fun representEntity(type: EntityType) {
					if (added.add(type.name)) {
						file.writeLine("class ${type.name};")
					}
				}
				
				override fun representEnum(type: EnumType) {}
				
				override fun representArray(type: ArrayType) {
					type.elementType.represent(this)
				}
			}
			
			forEach { it.type.represent(representer) }
		}
		
		file
			.writeLines(
				"class $name : public Entity",
				"{",
				"public:"
			)
			.addTab()
			.writeLines(
				"$name();",
				"virtual ~$name();",
				"",
				"static $name* create();",
				"static const int ID();",
				"",
				"void clear();",
				""
			)
		
		// fields get/set
		type.fields.forEach {
			val methodName = convertToCamel(it.name)
			file.writeLines(
				"void set$methodName(${it.type.presentInput()} val);",
				"${it.type.presentOutput()} get$methodName();",
				""
			)
		}
		
		file
			.writeLines(
				"// Entity protocol",
				"virtual void write(DataWriter* writer) override;",
				"virtual void read(DataReader* reader) override;",
				""
			)
			.removeTab()
			.writeLine("private:")
			.addTab()
			.writeLine("static const int _ID = ${type.id};")
			.writeLine()
		
		// fields private
		type.fields.forEach {
			file.writeLine("${it.type.presentInput().replace("&", "")} _${it.name};")
		}
		
		file.removeTab()
			.writeLine("};")
		
		return file
	}
	
	private fun buildEntityFileC(type: EntityType): CodeFile {
		val file = CodeFile()
		
		val name = type.name
		
		file.writeLine("#include \"$name.h\"")
		
		type.fields.apply {
			val representer = object : TypeRepresenter<Unit> {
				private val added = mutableSetOf<String>()
				
				override fun representPrimitive(type: PrimitiveType) {}
				
				override fun representEntity(type: EntityType) {
					if (added.add(type.name)) {
						file.writeLine("#include \"${type.name}.h\"")
					}
				}
				
				override fun representArray(type: ArrayType) {
					type.elementType.represent(this)
				}
				
				override fun representEnum(type: EnumType) {}
			}
			forEach { it.type.represent(representer) }
		}
		
		file.writeLine("")
			.write("$name::$name()")
		
		// constructor fields
		val list = type.fields.filterNot { it.type is ArrayType }
		if (list.isEmpty()) {
			file.writeLine()
		} else {
			file.writeLine(" :")
			file.writeSeparated(list, {
				if (it.type.isEnum) {
					file.write("_${it.name}(${it.type.name}::${it.type.asEnumType().values.first()})")
				} else {
					file.write("_${it.name}(${it.type.presentDefault()})")
				}
			}, ",\n")
		}
		file
			.writeLines(
				"",
				"{"
			)
			.addTab()
		// constructor safe fields
		type.fields
			.filter { it.type.let { it is ArrayType && it.elementType.isEntity } }
			.forEach {
				file.writeLine("_${it.name} = __Array::create();")
				file.writeLine("CC_SAFE_RETAIN(_${it.name});")
			}
		file
			.writeLine("setId(ID());")
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		file
			.writeLine("$name::~$name()")
			.writeLine("{")
			.addTab()
			.writeLine("clear();")
			.writeLine()
		// clear safe fields
		type.fields.filter { it.type.let { it is ArrayType && it.elementType.isEntity } }
			.forEach {
				file.writeLine("CC_SAFE_RELEASE(_${it.name});")
			}
		file
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		// ID()
		file
			.writeLine("const int $name::ID()")
			.writeLine("{")
			.addTab()
			.writeLine("return _ID;")
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		// create()
		file
			.writeLine("$name* $name::create()")
			.writeLine("{")
			.addTab()
			.writeLine("auto pRet = new (std::nothrow) $name();")
			.writeLine("if (pRet)")
			.addTab()
			.writeLine("pRet->autorelease();")
			.removeTab()
			.writeLine("else")
			.addTab()
			.writeLine("CC_SAFE_DELETE(pRet);")
			.removeTab()
			.writeLine()
			.writeLine("return pRet;")
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		
		// clear()
		file
			.writeLine("void $name::clear()")
			.writeLine("{")
			.addTab()
		// clear fields
		type.fields.forEach {
			when {
				it.type is PrimitiveType -> file.writeLine("_${it.name} = ${it.type.presentDefault()};")
				it.type.isEntity         -> file.writeLine("CC_SAFE_RELEASE_NULL(_${it.name});")
				it.type.isEnum           -> file.writeLine("_${it.name} = ${it.type.name}::${it.type.asEnumType().values.first()};")
				it.type is ArrayType     -> {
					when ((it.type as ArrayType).elementType) {
						is PrimitiveType -> file.writeLine("_${it.name}.clear();")
						is StructureType -> file.writeLine("_${it.name}->removeAllObjects();")
						else             -> throw IllegalArgumentException()
					}
				}
				else                     -> throw IllegalArgumentException()
			}
		}
		file
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		// write()
		file
			.writeLine("void $name::write(DataWriter* writer)")
			.writeLine("{")
			.addTab()
		type.fields.forEach {
			val t = it.type
			
			if (t.isEnum) {
				file.write("writer->writeEnum((int)_${it.name}")
			} else {
				file.write("writer->write${t.presentCodec()}(_${it.name}")
				
				if (t is ArrayType && t.elementType is PrimitiveType) {
					file.write(", ${t.elementType.presentValueType()}")
				}
			}
			
			file.writeLine(");")
		}
		file
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		// read()
		file
			.writeLine("void $name::read(DataReader* reader)")
			.writeLine("{")
			.addTab()
		type.fields.forEach {
			val t = it.type
			
			if (t is ArrayType) {
				val elementType = t.elementType
				if (elementType is PrimitiveType) {
					file.writeLines(
						"_${it.name}.clear();",
						"reader->readRawArray(_${it.name}, ${elementType.presentValueType()});"
					)
				} else if (elementType.isEntity) {
					file.writeLines(
						"_${it.name}->removeAllObjects();",
						"reader->readObjectArray(_${it.name}, ${elementType.id});"
					)
				} else {
					throw IllegalArgumentException()
				}
			} else if (t.isEntity) {
				file.writeLines(
					"CC_SAFE_RELEASE(_${it.name});",
					"_${it.name} = static_cast<${t.name}*>(reader->readEntity(${t.id}));",
					"CC_SAFE_RETAIN(_${it.name});"
				)
			} else if (t.isEnum) {
				file.writeLine("_${it.name} = (${t.name})reader->readEnum();")
			} else {
				file.writeLine("_${it.name} = reader->read${t.presentCodec()}();")
			}
			
		}
		file
			.removeTab()
			.writeLine("}")
			.writeLine()
		
		// fields get/set
		type.fields.forEach {
			val n = it.name
			val m = convertToCamel(n)
			val t = it.type
			
			/// setter
			file
				.writeLines(
					"void $name::set$m(${t.presentInput()} val)",
					"{"
				)
				.addTab()
			when {
				t is PrimitiveType || t.isEnum -> file.writeLine("_$n = val;")
				t.isEntity                     -> file.writeLines(
					"CC_SAFE_RELEASE(_$n);",
					"_$n = val;",
					"CC_SAFE_RETAIN(_$n);"
				)
				t is ArrayType                 -> when (t.elementType) {
					is PrimitiveType -> file.writeLines(
						"_$n.clear();",
						"copy(val.begin(), val.end(), std::back_inserter(_$n));"
					)
					is StructureType -> file.writeLines(
						"_$n->removeAllObjects();",
						"_$n->addObjectsFromArray(val);"
					)
					else             -> throw IllegalArgumentException()
				}
				
				else                           -> throw IllegalArgumentException()
			}
			file
				.removeTab()
				.writeLines(
					"}",
					""
				)
			
			/// getter
			file
				.writeLines(
					"${t.presentOutput()} $name::get$m()",
					"{"
				)
				.addTab()
				.writeLine("return _$n;")
				.removeTab()
				.writeLines(
					"}",
					""
				)
		}
		
		return file
	}
	
	private val presenterValueType = object : TypeRepresenter<String> {
		override fun representPrimitive(type: PrimitiveType) = when (type) {
			PrimitiveType.BOOL   -> "Value::Type::BOOLEAN"
			PrimitiveType.INT    -> "Value::Type::INTEGER"
			PrimitiveType.LONG   -> "Value::Type::LONGLONG"
			PrimitiveType.DOUBLE -> "Value::Type::DOUBLE"
			PrimitiveType.STRING -> "Value::Type::STRING"
			else                 -> throw UnsupportedOperationException()
		}
	}
	
	private fun DataType.presentValueType(): String {
		return represent(presenterValueType)
	}
	
	private val presenterCodec = object : TypeRepresenter<String> {
		override fun representPrimitive(type: PrimitiveType) = when (type) {
			PrimitiveType.BOOL   -> "Bool"
			PrimitiveType.INT    -> "Int"
			PrimitiveType.LONG   -> "Long"
			PrimitiveType.DOUBLE -> "Double"
			PrimitiveType.STRING -> "String"
			else                 -> throw IllegalArgumentException()
		}
		
		override fun representEntity(type: EntityType) = "Entity"
		
		override fun representArray(type: ArrayType) = when (type.elementType) {
			is PrimitiveType -> "RawArray"
			is StructureType -> "ObjectArray"
			else             -> throw IllegalArgumentException()
		}
		
		override fun representEnum(type: EnumType) = "Enum"
	}
	
	private fun DataType.presentCodec(): String {
		return represent(presenterCodec)
	}
	
	private val presenterDefault = object : TypeRepresenter<String> {
		override fun representPrimitive(type: PrimitiveType) = when (type) {
			PrimitiveType.BOOL   -> "false"
			PrimitiveType.INT    -> "0"
			PrimitiveType.LONG   -> "0"
			PrimitiveType.DOUBLE -> "0.0"
			PrimitiveType.STRING -> "\"\""
			else                 -> throw UnsupportedOperationException()
		}
		
		override fun representEntity(type: EntityType) = "nullptr"
		
		override fun representEnum(type: EnumType) = "0"
	}
	
	private fun DataType.presentDefault(): String {
		return represent(presenterDefault)
	}
	
	private val presenterInput = object : TypeRepresenter<String> {
		override fun representPrimitive(type: PrimitiveType) = when (type) {
			PrimitiveType.BOOL   -> "bool"
			PrimitiveType.INT    -> "int"
			PrimitiveType.LONG   -> "long long"
			PrimitiveType.DOUBLE -> "double"
			PrimitiveType.STRING -> "string"
			else                 -> throw UnsupportedOperationException()
		}
		
		override fun representEntity(type: EntityType) = "${type.name}*"
		
		override fun representArray(type: ArrayType) = when (type.elementType) {
			is PrimitiveType -> "vector<Value>&"
			is StructureType -> "__Array*"
			else             -> throw UnsupportedOperationException()
		}
		
		override fun representEnum(type: EnumType) = type.name
	}
	
	private fun DataType.presentInput(): String {
		return represent(presenterInput)
	}
	
	private fun DataType.presentOutput(): String {
		return when (this) {
			PrimitiveType.STRING -> "string&"
			else                 -> presentInput()
		}
	}
	
}

private fun String.toUpperUnderscoreCase(): String {
	val v = this
	
	val result = StringBuffer()
	var begin = true
	var lastUppercase = false
	for (i in 0 until v.length) {
		val ch = v[i]
		if (Character.isUpperCase(ch)) {
			// is start?
			if (begin) {
				result.append(ch)
			} else {
				if (lastUppercase) {
					// test if end of acronym
					if (i + 1 < v.length) {
						val next = v[i + 1]
						if (Character.isUpperCase(next)) {
							// acronym continues
							result.append(ch)
						} else {
							// end of acronym
							result.append('_').append(ch)
						}
					} else {
						// acronym continues
						result.append(ch)
					}
				} else {
					// last was lowercase, insert _
					result.append('_').append(ch)
				}
			}
			lastUppercase = true
		} else {
			result.append(Character.toUpperCase(ch))
			lastUppercase = false
		}
		begin = false
	}
	return result.toString()
}