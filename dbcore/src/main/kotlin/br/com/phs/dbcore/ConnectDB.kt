package br.com.phs.dbcore

import org.sqlite.core.CoreResultSet
import java.sql.Connection
import java.sql.DriverManager

object ConnectDB {

    @JvmStatic
    fun executeQuery(dbPath: String, command: String): ResultObject {

        var conn: Connection? = null
        val columns: Array<String>?
        val resultList: MutableList<MutableList<Any>> = mutableListOf()
        val resultObject = ResultObject()

        try {

            conn = getSQLiteConnection(dbPath)
            println("Connection to SQLite has been established.")

            val stmt = conn.createStatement()

            if (!command.contains("select", true) && !command.contains("pragma", true)) {
                try {
                    stmt.execute(command)
                } catch (ex: Exception) {
                    resultObject.hasError = true
                    resultObject.errorMsg = ex.message?: "UNKNOWN Error"
                }

                resultObject.resultType = ResultTypeEnum.INSERT
                return resultObject
            }

            resultObject.resultType = ResultTypeEnum.SELECT

            val rs = stmt.executeQuery(command)

            var columnLabel = ""
            columns = (rs as CoreResultSet).cols
            columns.forEach { column ->
                columnLabel += " | $column"
            }
            columnLabel += " | "

            println(columnLabel)

            while (rs.next()) {

                val row: MutableList<Any> = mutableListOf()

                columns.forEach { column ->
                    val value = rs.getObject(column)
                    print(" | $value")
                    row.add(value?: "null")
                }
                resultList.add(row)
                println(" | ")
            }

            val list = mutableListOf<Array<Any>>()
            resultList.forEach {
                list.add(it.toTypedArray())
            }

            resultObject.result = list.toTypedArray()
            resultObject.columns = columns

        } catch (ex: Exception) {
            println("Error: ${ex.message}")
            resultObject.hasError = true
            resultObject.errorMsg = ex.message?: "UNKNOWN ERROR"
        } finally {
            conn?.close()
        }

        return resultObject
    }

    @JvmStatic
    private fun getSQLiteConnection(dbPath: String): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    @JvmStatic
    fun initMainDB() {
        try {
            val createCommandsTable =
                "CREATE TABLE IF NOT EXISTS historic (id TEXT PRIMARY KEY, date INTEGER, desc TEXT)"
            val conn = getSQLiteConnection("main.db")
            conn.createStatement().execute(createCommandsTable)
            conn.close()
        } catch (ex: Exception) {}
    }

    @JvmStatic
    fun insertHistoric(args: MutableList<Any>) {
        try {
            initMainDB()
            val conn = getSQLiteConnection("main.db")
            val sql = "INSERT INTO historic (id, date, desc) VALUES(?, ?, ?)"
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, args[0] as String)
            pstmt.setInt(2, (args[1] as Long).toInt())
            pstmt.setString(3, args[2] as String)
            pstmt.executeUpdate()
            conn.close()
        } catch (ex: Exception) {
            println("Error: ${ex.message}")
        }
    }

    @JvmStatic
    fun getTablesName(dbPath: String): List<String> {

        val tablesName = mutableListOf<String>()

        try {
            val conn = getSQLiteConnection(dbPath)
            val sql = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_metadata';"
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(sql)
            if (rs != null) {
                while (rs.next()) {
                    tablesName.add(rs.getString(1))
                }
            }
        } catch (ex: Exception) {
            println("Error: ${ex.message}")
        }

        return tablesName
    }

    @JvmStatic
    fun getPragma(dbPath: String, tableName: String): List<String> {

        val columns = mutableListOf<String>()
        val conn = getSQLiteConnection(dbPath)
        try {
            val sql = "pragma table_info('$tableName');"
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(sql)
            while (rs.next()) {
                val name = rs.getString(2)
                if (!name.isNullOrEmpty())
                    columns.add(name)
            }
        } catch (ex: Exception) {
            println("Error: ${ex.message}")
        } finally {
            conn.close()
        }

        return columns
    }

}