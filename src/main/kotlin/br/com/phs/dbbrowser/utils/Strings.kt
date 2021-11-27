package br.com.phs.dbbrowser.utils

const val FILE = 0
const val EDIT = 1
const val ABOUT = 2
const val SETTINGS = 3
const val EXIT = 4
const val SQL_CMD_LABEL = 5
const val SQL_RESULT_LABEL = 6
const val TERMINAL = 7
const val NEW = 8
const val OPEN_DB = 9
const val EXPORT_DATABASE  =10

fun getLabel(language: Int, index: Int): String {
    return when(language) {
        0 -> portuguese(index)
        1 -> english(index)
        2 -> spanish(index)
        else -> "Unknown"
    }
}

private fun portuguese(index: Int): String {

    return when(index) {
        FILE -> "Arquivo"
        EDIT -> "Editar"
        SETTINGS -> "Configurações"
        ABOUT -> "Sobre"
        EXIT -> "Sair"
        SQL_CMD_LABEL -> "Comando SQL"
        SQL_RESULT_LABEL -> "Resultado"
        TERMINAL -> "Terminal"
        NEW -> "Novo"
        OPEN_DB -> "Abrir BD"
        EXPORT_DATABASE -> "Exportar BD"
        else -> "Unknown"
    }

}

private fun english(index: Int): String {

    return when(index) {
        else -> "Unknown"
    }
}

private fun spanish(index: Int): String {

    return when (index) {
        else -> "Unknown"
    }
}