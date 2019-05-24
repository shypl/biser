package org.shypl.biser.compiler.builder.ts

import org.shypl.biser.compiler.Module
import org.shypl.biser.compiler.Utils.convertToCamel
import org.shypl.biser.compiler.builder.CodeFile
import org.shypl.biser.compiler.builder.ModuleBuilder
import org.shypl.biser.compiler.model.*
import java.nio.file.Path

class TsModuleBuilder : ModuleBuilder {
    override fun getLang() = "ts"

    override fun build(module: Module, model: Model) {

        val enumsFile = CodeFile()
        val entityFile = CodeFile()
        val entityFabricFile = CodeFile()

        entityFabricFile
                .writeLine("import Entity from \"./Entity\";")
                .writeLine()

        for (type in model.structures) {
            if (type is EntityType) {
                entityFabricFile
                        .writeLine("import ${type.name} from \"../csi/Objects/${type.name}\";")
            }
        }

        entityFabricFile
                .writeLines(
                        "",
                        "export default class EntityFabric",
                        "{")
                .addTab()
                .writeLines(
                        "",
                        "public static create(eId: number) : Entity",
                        "{")
                .addTab()


        for (type in model.structures) {
            if (type is EntityType) {
                entityFabricFile
                        .writeLine("if (eId == ${type.name}.ID)")
                        .addTab()
                        .writeLine("return new ${type.name}();")
                        .removeTab()

                buildEntity(module.target, type)
            } else if (type is EnumType) {
                buildEnum(enumsFile, type)
            }
        }

        entityFabricFile
                .writeLines(
                        "",
                        "return null;")
                .removeTab()
                .writeLines("}", "")
                .removeTab()
                .writeLine("}")

        entityFile
                .writeLines(
                        "",
                        "import DataWriter from \"./DataWriter\";",
                        "import DataReader from \"./DataReader\";",
                        "",
                        "export default class Entity",
                        "{")
                .addTab()
                .writeLines(
                        "protected _eId: number = -1;",
                        "")

        entityFile
                .writeLines(
                        "public setEId(value: number)",
                        "{",
                        "\tthis._eId = value;",
                        "}",
                        "",
                        "public getEId() : number",
                        "{",
                        "\treturn this._eId;",
                        "}",
                        "",
                        "public write(writer: DataWriter)",
                        "{",
                        "}",
                        "",
                        "public read(reader: DataReader)",
                        "{",
                        "}")
                .removeTab()
                .writeLine("}")

        enumsFile.save(module.target.resolve("csi/Objects/CsiEnums.ts"))
        entityFile.save(module.target.resolve("io/Entity.ts"))
        entityFabricFile.save(module.target.resolve("io/EntityFabric.ts"))

        val api = model.api
        if (api.hasServices()) {
            when (module.side) {
                ApiSide.CLIENT -> buildApi(module.target, api)
                else -> RuntimeException()
            }
        }
    }

    private fun buildEnum(file: CodeFile, type: EnumType) {
        file.writeLine("export enum ${type.name}")
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
                .writeLine("}")
                .writeLine()
    }

    private fun buildApi(dir: Path, api: Api) {
        buildApiDesc(api.services).save(dir.resolve("csi/Objects/CsiApiIds.ts"))
    }

    private fun buildApiDesc(services: Collection<ApiService>): CodeFile {
        val file = CodeFile()

        file.writeLines("", "export default class CsiApi", "{")
                .writeLine().addTab()

        file.writeLine("// Services")
        services.forEach {
            file.writeLine("public static readonly CSI_SERVICE_${it.name.toUpperUnderscoreCase()} = ${it.id};")
        }
        file.writeLine()

        file.writeLine("// > Server actions")
        services.forEach { s ->
            if (s.hasServerActions()) {
                s.serverActions.forEach {
                    file.writeLine("public static readonly CSI_SERVER_${s.name.toUpperUnderscoreCase()}_${it.name.toUpperUnderscoreCase()} = ${it.id};")
                    if (it.hasResult()) {
                        file.writeLine("public static readonly CSI_SERVER_${s.name.toUpperUnderscoreCase()}_${it.name.toUpperUnderscoreCase()}_CALLBACK = ${100 * s.id + it.id};")
                    }
                }
                file.writeLine()
            }
        }


        file.writeLine("// < Client actions")
        services.forEach { s ->
            if (s.hasClientActions()) {
                s.clientActions.forEach {
                    file.writeLine("public static readonly CSI_CLIENT_${s.name.toUpperUnderscoreCase()}_${it.name.toUpperUnderscoreCase()} = ${it.id};")
                }
                file.writeLine()
            }
        }

        file.writeLine().removeTab()
        file.writeLine("}")

        return file
    }

    private fun buildEntity(dir: Path, type: EntityType) {
        buildEntityFile(type).save(dir.resolve("csi/Objects/${type.name}.ts"))
    }

    private fun buildEntityFile(type: EntityType): CodeFile {
        val file = CodeFile()

        type.children

        val name = type.name

        file
                .writeLines(
                        "import Entity from \"../../io/Entity\";",
                        "import DataWriter from \"../../io/DataWriter\";",
                        "import DataReader from \"../../io/DataReader\";",
                        "import { DataTypes } from \"../../io/DataTypes\";",
                        "import * as bigInt from \"big-integer/BigInteger\";"
                ).writeLine()

        type.fields.filter { it.type.isEnum }.apply {
            if (this.isNotEmpty())
                file.writeLine(this.joinToString (prefix = "import { ", postfix = " } from \"./CsiEnums\"") { it.type.name })
        }

        type.fields.apply {
            val representer = object : TypeRepresenter<Unit> {
                private val added = mutableSetOf<String>()

                override fun representPrimitive(type: PrimitiveType) {}

                override fun representEntity(type: EntityType) {
                    if (added.add(type.name)) {
                        file.writeLine("import ${type.name} from \"./${type.name}\";")
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
                .writeLine()
                .writeLines("export default class $name extends Entity", "{")
                .addTab()
                .writeLine("static readonly ID: number = ${type.id};")
                .writeLine()

        // fields private
        type.fields.forEach {
            file.writeLine("public ${it.name}: ${it.type.presentInput()} = ${it.type.presentDefault()};")
        }

        // constructor
        file
                .writeLine()
                .writeLines("constructor()", "{")
                .addTab()
                .writeLines("super();", "", "this.setEId($name.ID);")
                .removeTab()
                .writeLine("}")

        // write()
        file
                .writeLine()
                .writeLine("public write(writer: DataWriter)")
                .writeLine("{")
                .addTab()

        type.fields.forEach {
            val t = it.type

            if (t.isEnum) {
                file.write("writer.writeEnum(this.${it.name}")
            } else {
                file.write("writer.write${t.presentCodec()}(this.${it.name}")

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
                .writeLine("public read(reader: DataReader)")
                .writeLine("{")
                .addTab()

        type.fields.forEach {
            val t = it.type

            if (t is ArrayType) {
                val elementType = t.elementType
                if (elementType is PrimitiveType) {
                    file.writeLines(
                            "this.${it.name} = reader.readRawArray(${elementType.presentValueType()});"
                    )
                } else if (elementType.isEntity) {
                    file.writeLines(
                            "this.${it.name} = <${t.name}[]>reader.readObjectArray(${t.name}.ID);"
                    )
                } else {
                    throw IllegalArgumentException()
                }
            } else if (t.isEntity) {
                file.writeLine("this.${it.name} = <${t.name}>(reader.readEntity(${t.name}.ID));")
            } else if (t.isEnum) {
                file.writeLine("this.${it.name} = <${t.name}>reader.readEnum();")
            } else {
                file.writeLine("this.${it.name} = reader.read${t.presentCodec()}();")
            }

        }
        file
                .removeTab()
                .writeLine("}")
                .writeLine()

        file.removeTab()
                .writeLine("}")
                .writeLine()

        return file
    }

    private val presenterValueType = object : TypeRepresenter<String> {
        override fun representPrimitive(type: PrimitiveType) = when (type) {
            PrimitiveType.BYTE -> "DataTypes.BYTE"
            PrimitiveType.BOOL -> "DataTypes.BOOLEAN"
            PrimitiveType.INT -> "DataTypes.INTEGER"
            PrimitiveType.LONG -> "DataTypes.LONGLONG"
            PrimitiveType.DOUBLE -> "DataTypes.DOUBLE"
            PrimitiveType.STRING -> "DataTypes.STRING"
            else -> throw UnsupportedOperationException()
        }
    }

    private fun DataType.presentValueType(): String {
        return represent(presenterValueType)
    }

    private val presenterCodec = object : TypeRepresenter<String> {
        override fun representPrimitive(type: PrimitiveType) = when (type) {
            PrimitiveType.BYTE -> "Int"
            PrimitiveType.BOOL -> "Bool"
            PrimitiveType.INT -> "Int"
            PrimitiveType.LONG -> "Long"
            PrimitiveType.DOUBLE -> "Double"
            PrimitiveType.STRING -> "String"
            else -> throw IllegalArgumentException()
        }

        override fun representEntity(type: EntityType) = "Entity"

        override fun representArray(type: ArrayType) = when (type.elementType) {
            is PrimitiveType -> "RawArray"
            is StructureType -> "ObjectArray"
            else -> throw IllegalArgumentException()
        }

        override fun representEnum(type: EnumType) = "Enum"
    }

    private fun DataType.presentCodec(): String {
        return represent(presenterCodec)
    }

    private val presenterDefault = object : TypeRepresenter<String> {
        override fun representPrimitive(type: PrimitiveType) = when (type) {
            PrimitiveType.BYTE -> "0"
            PrimitiveType.BOOL -> "false"
            PrimitiveType.INT -> "0"
            PrimitiveType.LONG -> "bigInt(0)"
            PrimitiveType.DOUBLE -> "0.0"
            PrimitiveType.STRING -> "\"\""
            else -> throw UnsupportedOperationException()
        }

        override fun representEntity(type: EntityType) = "null"

        override fun representEnum(type: EnumType) = "0"

        override fun representArray(type: ArrayType) = "[]"
    }

    private fun DataType.presentDefault(): String {
        return represent(presenterDefault)
    }

    private val presenterInput = object : TypeRepresenter<String> {
        override fun representPrimitive(type: PrimitiveType) = when (type) {
            PrimitiveType.BYTE -> "number"
            PrimitiveType.BOOL -> "boolean"
            PrimitiveType.INT -> "number"
            PrimitiveType.LONG -> "bigInt.BigInteger"
            PrimitiveType.DOUBLE -> "number"
            PrimitiveType.STRING -> "string"
            else -> throw UnsupportedOperationException()
        }

        override fun representEntity(type: EntityType) = "${type.name}"

        override fun representArray(type: ArrayType) = when (type.elementType) {
            is PrimitiveType -> representPrimitive(type.elementType as PrimitiveType) + "[]"
            is StructureType -> "${type.name}[]"
            else -> throw UnsupportedOperationException()
        }

        override fun representEnum(type: EnumType) = type.name
    }

    private fun DataType.presentInput(): String {
        return represent(presenterInput)
    }

    private fun DataType.presentOutput(): String {
        return when (this) {
            PrimitiveType.STRING -> "string"
            else -> presentInput()
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