package br.com.phs.dbbrowser.data.models

data class ApplicationConfig(
    var language: Int = 0,
    var sqlKeyWordEnabled: Boolean = false,
    var tablesKeyWordEnabled: Boolean = false,
    var columnsKeyWordEnabled: Boolean = false
) {
    companion object {
        const val LANGUAGE = "language"
        const val SQL_KEYWORD_ENABLED = "sql_keyword_enabled"
        const val TABLES_KEYWORD_ENABLED = "tables_keyword_enabled"
        const val COLUMNS_KEYWORD_ENABLED = "columns_keyword_enabled"
    }

    constructor(appProperties: Map<String, Any>): this() {
        this.language = (appProperties[LANGUAGE]?: "0").toString().toInt()
        this.sqlKeyWordEnabled = (appProperties[SQL_KEYWORD_ENABLED]?: "false").toString().toBoolean()
        this.tablesKeyWordEnabled = (appProperties[TABLES_KEYWORD_ENABLED]?: "false").toString().toBoolean()
        this.columnsKeyWordEnabled = (appProperties[COLUMNS_KEYWORD_ENABLED]?: "false").toString().toBoolean()
    }
}
