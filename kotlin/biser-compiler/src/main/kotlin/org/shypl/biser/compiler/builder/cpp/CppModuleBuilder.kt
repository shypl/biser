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
		
		for (type in model.structures) {
			if (type is EntityType) {
				buildEntity(module.target, type)
			} else if (type is EnumType) {
				TODO()
			}
		}
		
		val api = model.api
		if (api.hasServices()) {
			when (module.side) {
				ApiSide.CLIENT -> buildApi(module.target, api)
				else           -> RuntimeException()
			}
		}
	}
	
	private fun buildApi(dir: Path, api: Api) {
		buildApiDesc(api.services).save(dir.resolve("_api.txt"))
	}
	
	private fun buildApiDesc(services: Collection<ApiService>): CodeFile {
		val file = CodeFile()
		
		services.forEach {
			file.writeLine("${it.name} : ${it.id} {")
				.addTab()
			
			it.serverActions.forEach {
				file.writeLine(" > ${it.name} : ${it.id}")
			}
			
			file.writeLine()
			
			it.clientActions.forEach {
				file.writeLine(" < ${it.name} : ${it.id}")
			}
			
			file.removeTab()
				.writeLine("}")
				.writeLine()
		}
		
		return file
	}
	
	private fun buildEntity(dir: Path, type: EntityType) {
		buildEntityFileH(type).save(dir.resolve("${type.name}.h"))
		buildEntityFileC(type).save(dir.resolve("${type.name}.cpp"))
	}
	
	private fun buildEntityFileH(type: EntityType): CodeFile {
		val file = CodeFile()
		
		val name = type.name
		
		file
			.writeLines(
				"USING_NS_CC;",
				"USING_NS_BISER_IO;",
				"using namespace std;",
				"",
				"class $name public Entity",
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
			file.writeLine("${it.type.presentInput()} _${it.name};")
		}
		
		file.removeTab()
			.writeLine("};")
		
		return file
	}
	
	private fun buildEntityFileC(type: EntityType): CodeFile {
		val file = CodeFile()
		
		val name = type.name
		
		file.writeLines(
			"#include \"$name.h\"",
			"",
			"$name::$name() :"
		)
		
		// constructor fields
		file.writeSeparated(type.fields.filterNot { it.type is ArrayType }, {
			file.write("_${it.name}(${it.type.presentDefault()})")
		}, ",\n")
		
		file
			.writeLines(
				"",
				"{"
			)
			.addTab()
		// constructor safe fields
		type.fields.filter { it.type.let { it is ArrayType && it.elementType is StructureType } }
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
		type.fields.filter { it.type.let { it is ArrayType && it.elementType is StructureType } }
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
			when (it.type) {
				is PrimitiveType -> file.writeLine("_${it.name} = ${it.type.presentDefault()};")
				is StructureType -> file.writeLine("CC_SAFE_RELEASE_NULL(_${it.name});")
				is ArrayType     -> {
					when ((it.type as ArrayType).elementType) {
						is PrimitiveType -> file.writeLine("_${it.name}.clear();")
						is StructureType -> file.writeLine("_${it.name}->removeAllObjects();")
						else             -> throw IllegalArgumentException()
					}
				}
				else             -> throw IllegalArgumentException()
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
			
			file.write("writer->write${t.presentCodec()}(_${it.name}")
			
			if (t is ArrayType && t.elementType is PrimitiveType) {
				file.write(", ${t.elementType.presentValueType()}")
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
				} else if (elementType is StructureType) {
					file.writeLines(
						"_${it.name}->removeAllObjects();",
						"reader->readObjectArray(_${it.name}, ${elementType.id});"
					)
				} else {
					throw IllegalArgumentException()
				}
			} else if (t is StructureType) {
				file.writeLines(
					"CC_SAFE_RELEASE(_${it.name});",
					"_${it.name} = reader->readEntity(${t.id});",
					"CC_SAFE_RETAIN(_${it.name});"
				)
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
			when (t) {
				is PrimitiveType -> file.writeLine("_$n = val;")
				is StructureType -> file.writeLines(
					"CC_SAFE_RELEASE(_$n);",
					"_$n = val;",
					"CC_SAFE_RETAIN(_$n);"
				)
				is ArrayType     -> when (t.elementType) {
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
				
				else             -> throw IllegalArgumentException()
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
					"${t.presentOutput()} get$m()",
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
		
		override fun representEntity(type: EntityType) = throw UnsupportedOperationException()
		
		override fun representEnum(type: EnumType) = throw UnsupportedOperationException()
		
		override fun representArray(type: ArrayType) = throw UnsupportedOperationException()
		
		override fun representMap(type: MapType) = throw UnsupportedOperationException()
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
		
		override fun representEnum(type: EnumType) = throw UnsupportedOperationException()
		
		override fun representArray(type: ArrayType) = when (type.elementType) {
			is PrimitiveType -> "RawArray"
			is StructureType -> "ObjectArray"
			else             -> throw IllegalArgumentException()
		}
		
		override fun representMap(type: MapType) = throw UnsupportedOperationException()
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
		
		override fun representEnum(type: EnumType) = throw UnsupportedOperationException()
		
		override fun representArray(type: ArrayType) = throw UnsupportedOperationException()
		
		override fun representMap(type: MapType) = throw UnsupportedOperationException()
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
		
		override fun representEnum(type: EnumType) = throw UnsupportedOperationException()
		
		override fun representArray(type: ArrayType) = when (type.elementType) {
			is PrimitiveType -> "vector<Value>&"
			is StructureType -> "__Array*"
			else             -> throw UnsupportedOperationException()
		}
		
		override fun representMap(type: MapType) = throw UnsupportedOperationException()
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
