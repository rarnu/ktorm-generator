import com.isyscore.kotlin.common.int
import com.isyscore.kotlin.common.string
import java.io.File
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types

object Database {

    val driverMap = mapOf(
        0 to "com.mysql.cj.jdbc.Driver",    // mysql
        1 to "oracle.jdbc.OracleDriver", // oracle
        2 to "com.microsoft.sqlserver.jdbc.SQLServerDriver", // sql server
        3 to "org.postgresql.Driver", // postgresql
        4 to "org.sqlite.JDBC", // sqlite
        5 to "dm.jdbc.driver.DmDriver", // 达梦
    )

    fun connect(driverType: Int, jdbcUrl: String, user: String, password: String, callback: (succ: Boolean, conn: Connection?, error: String?) -> Unit) {
        val driverClass = driverMap[driverType]
        Class.forName(driverClass)
        var err: String? = null
        val conn = try {
            DriverManager.getConnection(jdbcUrl, user, password)
        } catch (e: Exception) {
            err = e.message
            null
        }
        callback(conn != null, conn, err)
    }

    fun exportCode(pkgName: String, conn: Connection, dir: File, callback: (total: Int, exported: Int) -> Unit) {
        val tableList = conn.getTableList()
        val total = tableList.size
        var doneCount = 0
        for (table in tableList) {
            val cols = conn.getTableInfo(table)
            CodeGenerator.generateCode(pkgName, table, cols, dir)
            doneCount++
        }
        callback(total, doneCount)
    }

    private fun Connection.getTableList(): List<String> =
        metaData.getTables(catalog, schema, null, null).use { resultSet ->
            val list = mutableListOf<String>()
            while (resultSet.next()) {
                if (resultSet.string("TABLE_TYPE") == "TABLE") {
                    list.add(resultSet.string("TABLE_NAME"))
                }
            }
            list
        }

    private fun Connection.getTableInfo(tableName: String): List<DBColumn> {
        val list = mutableListOf<DBColumn>()
        metaData.getTables(catalog, schema, tableName, null).use { rs ->
            // Types.VARCHAR
            val listPk = mutableListOf<String>()
            if (rs.next()) {
                // 获取主键
                metaData.getPrimaryKeys(catalog, schema, tableName).use { rsPk ->
                    while (rsPk.next()) {
                        listPk.add(rsPk.string("COLUMN_NAME"))
                    }
                }
                // 获取列
                metaData.getColumns(catalog, schema, tableName, null).use { rsCol ->
                    while (rsCol.next()) {
                        val colName = rsCol.string("COLUMN_NAME")
                        val primary = listPk.contains(colName)
                        list.add(DBColumn(colName, rsCol.int("DATA_TYPE"), primary))
                    }
                }
            }
        }
        Types.VARCHAR
        return list
    }

}