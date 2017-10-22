@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.Logger
import java.security.MessageDigest
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 * 定义一系列拓展，
 * 比如slf4j的，主要就是先判断再执行lambda,
 * Created by AoEiuV020 on 2017.10.02-16:01:09.
 */

inline fun Logger.trace(message: () -> Any?) {
    if (isTraceEnabled) {
        trace("{}", message().toString())
    }
}

inline fun Logger.debug(message: () -> Any?) {
    if (isDebugEnabled) {
        debug("{}", message().toString())
    }
}

inline fun Logger.info(message: () -> Any?) {
    if (isInfoEnabled) {
        info("{}", message().toString())
    }
}

inline fun Logger.warn(message: () -> Any?) {
    if (isWarnEnabled) {
        warn("{}", message().toString())
    }
}

inline fun Logger.error(message: () -> Any?) {
    if (isErrorEnabled) {
        error("{}", message().toString())
    }
}

inline fun Logger.error(e: Throwable, message: () -> Any?) {
    if (isErrorEnabled) {
        error(message().toString(), e)
    }
}

fun String.pick(pattern: Pattern): List<String> {
    val matcher = pattern.matcher(this)
    return if (matcher.find()) {
        List(matcher.groupCount()) {
            matcher.group(it + 1)
        }
    } else {
        throw IllegalStateException("not match <$this, $pattern>")
    }
}

/**
 * 默认识别换行，整个字符串作为整体，
 */
fun String.pick(pattern: String) = pick(Pattern.compile(pattern, Pattern.DOTALL))

fun Element.src(): String = attr("src")
fun Element.absSrc(): String = absUrl("src")
fun Element.href(): String = attr("href")
fun Element.absHref(): String = absUrl("href")
fun Element.title(): String = attr("title")
fun Element.textList(): List<String> = childNodes().mapNotNull {
    (it as? TextNode)?.wholeText?.takeIf { it.isNotBlank() }?.trim()
}

fun GsonBuilder.paNovel(): GsonBuilder = apply {
    Requester.attach(this)
}

@Suppress("FunctionName")
fun NovelContext.NovelSearch(name: String, url: String) = NovelGenre(name, SearchListRequester(url))

fun md5Hex(str: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(str.toByteArray(charset("UTF-8")))
    val stringBuilder = StringBuilder(digest.size * 2)
    for (b in digest) {
        if (b.toInt() and 255 < 16) {
            stringBuilder.append("0")
        }
        stringBuilder.append(Integer.toHexString(b.toInt() and 255))
    }
    return stringBuilder.toString()
}

fun des3(str: String): String {
    val generateSecret = SecretKeyFactory.getInstance("desede").generateSecret(DESedeKeySpec("JVYW9BWG7XJ98B3W34RT33B3".toByteArray()))
    val instance = Cipher.getInstance("desede/CBC/PKCS5Padding")
    instance.init(1, generateSecret, IvParameterSpec("01234567".toByteArray()))
    return base64(instance.doFinal(str.toByteArray(charset("utf-8"))))
}

fun base64(bArr: ByteArray): String {
    val a = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()
    val length = bArr.size
    val stringBuffer = StringBuilder(bArr.size * 3 / 2)
    val i = length - 3
    var i2 = 0
    var i3 = 0
    while (i3 <= i) {
        var i4 = bArr[i3].toInt() and 255 shl 16 or (bArr[i3 + 1].toInt() and 255 shl 8) or (bArr[i3 + 2].toInt() and 255)
        stringBuffer.append(a[i4 shr 18 and 63])
        stringBuffer.append(a[i4 shr 12 and 63])
        stringBuffer.append(a[i4 shr 6 and 63])
        stringBuffer.append(a[i4 and 63])
        i4 = i3 + 3
        i3 = i2 + 1
        if (i2 >= 14) {
            stringBuffer.append(" ")
            i3 = 0
        }
        i2 = i3
        i3 = i4
    }
    if (i3 == length - 2) {
        i3 = bArr[i3 + 1].toInt() and 255 shl 8 or (bArr[i3].toInt() and 255 shl 16)
        stringBuffer.append(a[i3 shr 18 and 63])
        stringBuffer.append(a[i3 shr 12 and 63])
        stringBuffer.append(a[i3 shr 6 and 63])
        stringBuffer.append("=")
    } else if (i3 == length - 1) {
        i3 = bArr[i3].toInt() and 255 shl 16
        stringBuffer.append(a[i3 shr 18 and 63])
        stringBuffer.append(a[i3 shr 12 and 63])
        stringBuffer.append("==")
    }
    return stringBuffer.toString()
}
