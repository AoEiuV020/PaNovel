package cc.aoeiuv020.base.jar

import java.security.MessageDigest

/**
 * Created by AoEiuV020 on 2018.04.18-10:19:39.
 */

/**
 * 返回utf-8转md5转16进制的小写，
 */
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.hex()
}

fun ByteArray.hex(): String = joinToString("") {
    "%02x".format(it)
}

fun String.hex(): String = toByteArray().hex()
