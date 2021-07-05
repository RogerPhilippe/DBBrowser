package br.com.phs.dbbrowser.utils

import java.text.SimpleDateFormat
import java.util.*

fun String.toEpochMilli(): Long {

    if (this.isEmpty())
        return 0L
    return SimpleDateFormat("dd/MM/yyyy").parse(this)!!.time
}

fun Date.toStringFormatted(): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(this)
}

