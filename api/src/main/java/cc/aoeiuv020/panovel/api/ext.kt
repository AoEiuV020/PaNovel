@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.base.jar.pick
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2017.10.02-16:01:09.
 */

/**
 * 结尾不要斜杆/，因为有的地址可能整数后面接文件后缀.html,
 * 开头要有斜杆/，因为有的网站可能host有整数，
 */
val firstIntPattern: Pattern = Pattern.compile("/(\\d+)", Pattern.DOTALL)

fun findFirstOneInt(url: String): String = path(url).pick(firstIntPattern).first()

val firstTwoIntPattern: Pattern = Pattern.compile("/(\\d+/\\d+)", Pattern.DOTALL)
fun findFirstTwoInt(url: String): String = url.pick(firstTwoIntPattern).first()

val firstThreeIntPattern: Pattern = Pattern.compile("/(\\d+/\\d+/\\d+)", Pattern.DOTALL)
fun findThreeTwoInt(url: String): String = url.pick(firstTwoIntPattern).first()

/**
 * 地址仅路径，斜杆/开头，
 */
fun path(url: String): String = try {
    URL(url).path
} catch (e: MalformedURLException) {
    url
}

inline fun <T> tryOrNul(block: () -> T?): T? = try {
    block()
} catch (e: Exception) {
    null
}