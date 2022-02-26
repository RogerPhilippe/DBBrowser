package br.com.phs.dbbrowser.data.models

import org.json.JSONObject

data class SQLiteRTResult(
    val status: Boolean,
    val result: JSONObject?
)
