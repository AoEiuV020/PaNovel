package cc.aoeiuv020.base.jar

import java.security.MessageDigest

/**
 * Created by AoEiuV020 on 2018.04.18-10:19:39.
 */
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}
