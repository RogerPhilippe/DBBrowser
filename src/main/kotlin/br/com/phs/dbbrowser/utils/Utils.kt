package br.com.phs.dbbrowser.utils

import java.awt.Image
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.*

const val SQL_KEYWORDS = 0

fun writeLastContent(content: String) {

    if (content.isEmpty()) {
        if (File("last_content.sql").exists())
            File("last_content.sql").delete()
        return
    }

    val file = File("last_content.sql")
    FileOutputStream(file).use {
        it.write(content.toByteArray())
    }

}

fun readLastContent(): String {
    if (!File("last_content.sql").exists())
        return ""
    val file = File("last_content.sql")
    return String(Files.readAllBytes(file.toPath()))
}

fun getKeywordsArray(): List<String> {
    if (!File("sql_keywords").exists())
        return listOf()
    val file = File("sql_keywords")
    return String(Files.readAllBytes(file.toPath())).split(";")
}

fun setOnProperties(properties: Map<String, Any>) {

    if (properties.isEmpty())
        return

    val file = File("./resource/application.properties")
    val prop = Properties()

    FileInputStream(file).use {
        prop.load(it)

        properties.forEach { property ->

            prop.setProperty(property.key, property.value as String)
        }

        val out: OutputStream = FileOutputStream(file)
        prop.store(out, "Application Properties")

    }

}

fun getFromProperties(): Map<String, Any> {

    var propertiesMap = hashMapOf<String, Any>()

    val file = File("./resource/application.properties")
    val prop = Properties()
    FileInputStream(file).use { prop.load(it) }

    prop.stringPropertyNames()
            .associateWith { prop.getProperty(it) }
            .forEach { resultMap ->
                propertiesMap[resultMap.key] = resultMap.value
            }

    return propertiesMap
}

fun getScaledImage(img: Image, w: Int, h: Int): Image {
    return img.getScaledInstance(w, h, Image.SCALE_SMOOTH)
}

fun md5(input:String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun getRandomHash(): String {
    return md5(Calendar.getInstance().timeInMillis.toString())
}

fun String.getLines(separator: String = ";"): List<String> {
    return this.split(separator)
}

fun <T> concatenate(vararg lists: List<T>): List<T> {
    return listOf(*lists).flatten()
}