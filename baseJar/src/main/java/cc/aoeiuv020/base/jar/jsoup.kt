@file:Suppress("unused")

package cc.aoeiuv020.base.jar

import okhttp3.Call
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import java.net.URL
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.10-15:56:52.
 */

fun jsoupParse(call: Call): Document {
    val response = call.execute()
    return response.body().notNull().use {
        it.byteStream().use { input ->
            Jsoup.parse(input, response.charset(), response.url())
        }
    }
}

fun jsoupConnect(url: String): Document = jsoupParse(get(url))

fun Element.findAll(predicate: (Element) -> Boolean): List<Element> {
    val list = LinkedList<Element>()
    NodeTraversor(object : NodeVisitor {
        override fun tail(node: Node?, depth: Int) {
        }

        override fun head(node: Node?, depth: Int) {
            if (node is Element && predicate(node)) {
                list.add(node)
            }
        }

    }).traverse(this)
    // 转成RandomAccess的ArrayList,
    return list.toList()
}

/**
 * 匹配空白符和空格符，
 * 和kotlin的trim效果一致，
 * javaWhitespace能匹配全角空格，
 * javaSpaceChar能匹配utf-8扩充的半角空格，
 */
private val whitespaceRegex = compileRegex("[\\p{javaWhitespace}\\p{javaSpaceChar}]+")
private val newLineRegex = compileRegex("[\n\r]+")

/**
 * 得到列表中每个元素的文字，包括子元素，
 * 所有文字部分按空白字符分割，这是最常有的情况，
 */
fun Elements.textListSplitWhitespace(): List<String> = flatMap { it.textListSplitWhitespace() }

/**
 * 同时添加了图片，markdown格式，
 */
fun Element.textList(): List<String> {
    // 用LinkedList方便频繁添加，
    val list = LinkedList<String>()
    val line = StringBuilder()
    NodeTraversor(object : NodeVisitor {
        override fun head(node: Node?, depth: Int) {
            if (node is TextNode) {
                if (preserveWhitespace(node.parentNode())) {
                    // 如果是需要保持空格的标签中的文本，按换行符拆成多行，
                    node.ownTextList().toCollection(list)
                } else {
                    // 如果是普通的标签中的文本，缓存起来，不算一行，
                    line.append(node.text())
                }
            } else if (node is Element) {
                // 添加图片，按自己的格式，
                if (node.isImage()) {
                    imgText(node)?.let { list.add(it) }
                }
                // 如果需要换行，把存起来的line处理掉，
                if (line.isNotBlank() && (node.isBr() || node.isBlock)) {
                    list.add(line.toString().trim())
                    line.delete(0, line.length)
                }
            }
        }

        override fun tail(node: Node?, depth: Int) {
        }
    }).traverse(this)
    if (line.isNotBlank()) {
        list.add(line.toString().trim())
        line.delete(0, line.length)
    }
    // 转成RandomAccess的ArrayList,
    return list.toList()
}

private fun preserveWhitespace(node: Node?): Boolean {
    // looks only at this element and one level up, to prevent recursion & needless stack searches
    if (node != null && node is Element) {
        return node.tag().preserveWhitespace() || node.parent() != null && node.parent().tag().preserveWhitespace()
    }
    return false
}


/**
 * 用所有空格或空白符分割元素里的文字，
 * 支持全角空格，
 * 同时添加了图片，markdown格式，
 */
fun Element.textListSplitWhitespace(): List<String> {
    // 用LinkedList方便频繁添加，
    val list = LinkedList<String>()
    NodeTraversor(object : NodeVisitor {
        private val line = StringBuilder()
        override fun head(node: Node?, depth: Int) {
            if (node is TextNode) {
                // 完全分割所有空白，不需要被分割的span之类也会被分割，
                node.ownTextListSplitWhitespace().toCollection(list)
            } else if (node is Element && node.isImage()) {
                imgText(node)?.let { list.add(it) }
            }
        }

        override fun tail(node: Node?, depth: Int) {
        }
    }).traverse(this)
    // 转成RandomAccess的ArrayList,
    return list.toList()
}

// svg中有image标签，
fun Element.isImage() = tagName() == "img" || tagName() == "image"

fun Element.isBr() = tagName() == "br"

// 按markdown格式添加图片字符串，
fun imgText(img: Element): String? {
    // 延迟加载可能把地址放在data-original,
    return (img.absDataOriginal().takeIf(String::isNotBlank)
            ?: img.absSrc().takeIf(String::isNotBlank)
            // svg中的image标签有这个属性，
            ?: img.absXlinkHref().takeIf(String::isNotBlank)
            )?.let {
        "![img]($it)"
    }
}

/**
 * 并不获取子元素里的文字，
 * 支持全角空格，
 */
fun Element.ownTextListSplitWhitespace(): List<String> =
        this.textNodes().flatMap { it.ownTextListSplitWhitespace() }


/**
 * 并不获取子元素里的文字，
 * 支持全角空格，
 * 同时添加了图片，markdown格式，
 */
fun Element.ownTextListWithImage(): List<String> =
        this.childNodes().flatMap {
            if (it is TextNode) {
                it.ownTextListSplitWhitespace()
            } else if (it is Element && it.tagName() == "img") {
                imgText(it)?.let { listOf(it) }
                        ?: listOf()
            } else {
                listOf()
            }
        }

/**
 * 切开所有换行符，
 */
fun TextNode.ownTextList(): List<String> =
// 用wholeText才能拿到换行符，
        wholeText.trim().takeIf(String::isNotEmpty)?.splitNewLine()?.filter(String::isNotBlank)
                ?: listOf()

/**
 * 切开所有空白符，
 */
fun TextNode.ownTextListSplitWhitespace(): List<String> =
// trim里的判断和这个whitespaceRegex是一样的，
// trim后可能得到空字符串，判断一下，
        this.wholeText.trim().takeIf(String::isNotEmpty)?.splitWhitespace() ?: listOf()

fun String.splitWhitespace(): List<String> = this.split(whitespaceRegex)
fun String.splitNewLine(): List<String> = this.split(newLineRegex)

fun Element.src(): String = attr("src")
fun Element.absSrc(): String = absUrl("src")
fun Element.absDataOriginal(): String = absUrl("data-original")
fun Element.href(): String = attr("href")
fun Element.absHref(): String = absUrl("href")
fun Element.xlinkHref(): String = attr("xlink:href")
fun Element.absXlinkHref(): String = absUrl("xlink:href")

/**
 * 地址仅路径，斜杆/开头，
 */
fun Element.path(): String = path(absHref())

fun Element.title(): String = attr("title")
fun Element.ownerPath(): String = URL(ownerDocument().location()).path
// kotlin的trim有包括utf8的特殊的空格，和java的trim不重复，
fun TextNode.textNotBlank(): String? = this.text().trim().takeIf(String::isNotBlank)

fun Element.ownTextList(): List<String> = this.textNodes().flatMap { it.ownTextList() }
fun Element.ownLinesString(): String = ownTextListSplitWhitespace().joinToString("\n")
fun Element.linesString(): String = textListSplitWhitespace().joinToString("\n")
fun TextNode.ownLinesString(): String = ownTextListSplitWhitespace().joinToString("\n")
fun Node.text(): String = (this as TextNode).text()
