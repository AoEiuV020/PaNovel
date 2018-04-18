package cc.aoeiuv020.panovel.api

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

/**
 * Created by AoEiuV020 on 2018.04.18-10:26:59.
 */
fun Element.src(): String = attr("src")

fun Element.absSrc(): String = absUrl("src")
fun Element.href(): String = attr("href")
fun Element.absHref(): String = absUrl("href")
fun Element.title(): String = attr("title")
fun Element.textList(): List<String> = childNodes().mapNotNull {
    (it as? TextNode)?.wholeText?.takeIf { it.isNotBlank() }?.trim()
}
