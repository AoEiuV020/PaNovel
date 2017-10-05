@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.Logger
import java.util.regex.Pattern

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

fun String.pick(pattern: Pattern): List<String>? {
    val matcher = pattern.matcher(this)
    return if (matcher.find()) {
        List(matcher.groupCount()) {
            matcher.group(it + 1)
        }
    } else {
        null
    }
}

/**
 * 默认识别换行，整个字符串作为整体，
 */
fun String.pick(pattern: String) = pick(Pattern.compile(pattern, Pattern.DOTALL))

fun Element.src(): String = attr("src")
fun Element.absHref(): String = absUrl("href")
fun Element.title(): String = attr("title")
fun Element.textList(): List<String> = childNodes()
        .drop(9)
        .filter { it is TextNode }
        .map { it as TextNode }
        .filterNot { it.isBlank }
        .map { it.wholeText.trim() }

