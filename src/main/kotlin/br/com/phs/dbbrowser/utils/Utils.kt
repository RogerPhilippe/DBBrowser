package br.com.phs.dbbrowser.utils

import java.awt.Image
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


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