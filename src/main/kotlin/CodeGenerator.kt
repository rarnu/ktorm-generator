import com.isyscore.kotlin.common.join
import com.isyscore.kotlin.common.replaceTag
import com.isyscore.kotlin.common.toTitleUpperCase
import java.io.File
import java.sql.Types

object CodeGenerator {

    val typeMap = mapOf(
        // jdbcType to (KtormMethod join KotlinType join Package)
        Types.BOOLEAN to ("boolean" join "Boolean" join "kotlin.Boolean"),
        Types.INTEGER to ("int" join "Int" join "kotlin.Int"),
        Types.SMALLINT to ("short" join "Short" join "kotlin.Short"),
        Types.BIGINT to ("long" join "Long" join "kotlin.Long"),
        Types.FLOAT to ("float" join "Float" join "kotlin.Float"),
        Types.DOUBLE to ("double" join "Double" join "kotlin.Double"),
        Types.DECIMAL to ("decimal" join "BigDecimal" join "java.math.BigDecimal"),
        Types.VARCHAR to ("varchar" join "String" join "kotlin.String"),
        Types.LONGVARCHAR to ("text" join "String" join "kotlin.String"),
        Types.BLOB to ("blob" join "ByteArray" join "kotlin.ByteArray"),
        Types.BINARY to ("bytes" join "ByteArray" join "kotlin.ByteArray"),
        Types.TIMESTAMP to ("datetime" join "LocalDateTime" join "java.time.Instant"),
        Types.DATE to ("date" join "LocalDate" join "java.time.LocalDate"),
        Types.TIME to ("time" join "LocalTime" join "java.time.LocalTime"),
        Types.OTHER to ("uuid" join "UUID" join "java.util.UUID"),
        Types.BIT to ("short" join "Short" join "kotlin.Short"),
        Types.TINYINT to ("short" join "Short" join "kotlin.Short"),
        Types.REAL to ("double" join "Double" join "kotlin.Double"),
        Types.NUMERIC to ("double" join "Double" join "kotlin.Double"),
        Types.CHAR to ("varchar" join "String" join "kotlin.String"),
        Types.VARBINARY to ("bytes" join "ByteArray" join "kotlin.ByteArray"),
        Types.LONGVARBINARY to ("bytes" join "ByteArray" join "kotlin.ByteArray"),
        Types.CLOB to ("blob" join "ByteArray" join "kotlin.ByteArray"),
        Types.NCHAR to ("varchar" join "String" join "kotlin.String"),
        Types.NVARCHAR to ("varchar" join "String" join "kotlin.String"),
        Types.LONGNVARCHAR to ("text" join "String" join "kotlin.String"),
        Types.NCLOB to ("blob" join "ByteArray" join "kotlin.ByteArray"),
    )

    fun generateCode(pkgName: String, tableName: String, columns: List<DBColumn>, dir: File) {
        fun String.conv(upperFirst: Boolean = false): String {
            val tmp = split("_").joinToString(separator = "") { it.toTitleUpperCase() }
            return if (upperFirst) tmp else tmp[0].lowercase() + tmp.drop(1)
        }

        val className = tableName.conv(true)
        val instanceName = tableName.conv()

        val listFields = columns.map {
            "    var %s: %s".format(it.name.conv(), typeMap[it.jdbcType]?.second)
        }
        val listBindings = columns.map {
            """    var %s = %s("%s")%s.bindTo { it.%s }""".format(
                it.name.conv(), typeMap[it.jdbcType]?.first, it.name, if (it.isPrimary) ".primaryKey()" else "", it.name.conv()
            )
        }

        val entityCode = TEMP_ENTITY
            .replaceTag("{{packageName}}") { pkgName }
            .replaceTag("{{className}}") { className }
            .replaceTag("{{fields}}") { listFields.joinToString(separator = "\n") }

        val tableCode = TEMP_TABLE
            .replaceTag("{{packageName}}") { pkgName }
            .replaceTag("{{className}}") { className }
            .replaceTag("{{instanceName}}") { instanceName }
            .replaceTag("{{tableName}}") { tableName }
            .replaceTag("{{bindings}}") { listBindings.joinToString(separator = "\n") }

        val fEntity = File(dir, "entity").apply { if (!exists()) mkdirs() }
        val fTable = File(dir, "table").apply { if (!exists()) mkdirs() }
        File(fEntity, "${className}.kt").writeText(entityCode)
        File(fTable, "${className}s.kt").writeText(tableCode)
    }

    private val TEMP_ENTITY = """package {{packageName}}.entity
        |import org.ktorm.entity.Entity
        |import java.time.*
        |
        |interface {{className}}: Entity<{{className}}> {
        |    companion object : Entity.Factory<{{className}}>()
        |{{fields}}
        |}
    """.trimMargin()

    private val TEMP_TABLE = """package {{packageName}}.table
        |import {{packageName}}.entity.{{className}}
        |import org.ktorm.database.Database
        |import org.ktorm.entity.sequenceOf
        |import org.ktorm.schema.*
        |
        |object {{className}}s: Table<{{className}}>("{{tableName}}") {
        |{{bindings}}
        |}
        |
        |val Database.{{instanceName}}s get() = this.sequenceOf({{className}}s)
    """.trimMargin()
}
