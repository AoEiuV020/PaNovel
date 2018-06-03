package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.base.jar.compilePattern
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.path
import okhttp3.Call
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import java.io.InputStream
import java.net.URLEncoder
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.10-18:08:08.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class JsoupNovelContext : OkHttpNovelContext() {
    companion object {
        const val TAG_NOVEL_NAME = "小说名"
        const val TAG_NOVEL_LINK = "小说链接"
        const val TAG_AUTHOR_NAME = "作者名"
        const val TAG_SEARCH_RESULT_LIST = "搜索结果列表"
        const val TAG_INTRODUCTION = "简介"
        const val TAG_CHAPTER_LINK = "章节链接"
        const val TAG_CHAPTER_PAGE = "目录页"
        const val TAG_CONTENT = "正文"
        const val TAG_ELEMENT = "元素"
        const val TAG_LIST = "列表"
        const val TAG_STRING = "字符串"
        const val TAG_IMAGE = "图片"
        const val TAG_LENGTH = "长度"
        const val TAG_STATUS = "状态"
        const val TAG_UPDATE_TIME = "更新时间"
        const val TAG_GENRE = "类型"

        /**
         * 匹配空白符和空格符，
         * 和kotlin的trim效果一致，
         * javaWhitespace能匹配全角空格，
         * javaSpaceChar能匹配utf-8扩充的半角空格，
         */
        private val whitespaceRegex = Regex("[\\p{javaWhitespace}\\p{javaSpaceChar}]+")

        /**
         * 得到列表中每个元素的文字，包括子元素，
         * 所有文字部分按空白字符分割，这是最常有的情况，
         */
        fun textList(elements: Elements): List<String> = elements.flatMap { textList(it) }

        /**
         * 用所有空格或空白符分割元素里的文字，
         * 支持全角空格，
         */
        fun textList(element: Element): List<String> {
            // 用LinkedList方便频繁添加，
            val list = LinkedList<String>()
            NodeTraversor(object : NodeVisitor {
                override fun tail(node: Node?, depth: Int) {
                }

                override fun head(node: Node?, depth: Int) {
                    if (node is TextNode) {
                        ownTextList(node).toCollection(list)
                    }
                }

            }).traverse(element)
            // 转成RandomAccess的ArrayList,
            return list.toList()
        }

        /**
         * 并不获取子元素里的文字，
         * 支持全角空格，
         */
        fun ownTextList(element: Element): List<String> =
                element.textNodes().flatMap { ownTextList(it) }

        /**
         * 切开所有空白符，
         */
        fun ownTextList(node: TextNode): List<String> =
        // trim里的判断和这个whitespaceRegex是一样的，
        // trim后可能得到空字符串，判断一下，
                node.wholeText.trim().takeIf(String::isNotEmpty)?.split(whitespaceRegex) ?: listOf()
    }

    /**
     * 有的网站没有指定编码，只能在这里强行指定，
     * null表示用默认的，一般可以，
     * 有点混乱，一般在parse前指定，不用这里的，
     */
    protected open val charset: String? get() = null

    protected fun parse(extra: String): Document = parse(connect(absUrl(extra)))
    protected fun parse(input: InputStream, charset: String?, baseUri: String): Document = try {
        Jsoup.parse(input, charset, baseUri)
    } catch (e: Exception) {
        throw IllegalStateException("页面<$baseUri>解析失败，", e)
    }

    protected fun parse(call: Call, charset: String? = this.charset): Document {
        val response = response(call)
        // 用Jsoup解析okhttp得到的InputStream，
        // 编码如果指定，就用指定的，没有就从okhttp的response中拿，再没有传null, Jsoup会自己尝试解析，
        // 如果有301之类跳转，最终响应的地址作为Jsoup解析的baseUri, 主要应该只是从相对地址计算绝对地址时用到，
        return response.inputStream { input ->
            parse(input, charset ?: response.charset(), response.url())
        }
    }

    /**
     * 下一页相关的暂不支持，
     */
    override fun getNextPage(extra: String): String? = getNextPage(parse(extra))

    protected open fun getNextPage(root: Document): String? = null

    protected fun Element.src(): String = attr("src")
    protected fun Element.absSrc(): String = absUrl("src")
    protected fun Element.href(): String = attr("href")
    protected fun Element.absHref(): String = absUrl("href")
    /**
     * 地址仅路径，斜杆/开头，
     */
    protected fun Element.path(): String = path(absHref())

    protected fun Element.title(): String = attr("title")
    protected fun Elements.textList(): List<String> = flatMap { it.textList() }
    protected fun Element.textList(): List<String> = textList(this)
    protected fun Elements.ownTextList(): List<String> = flatMap { it.ownTextList() }
    protected fun Element.ownTextList(): List<String> = ownTextList(this)
    protected fun TextNode.ownTextList(): List<String> = ownTextList(this)
    protected fun Node.text(): String = (this as TextNode).text()

    private val replaceWhiteWithNewLineRegex = Regex("\\s+")
    protected fun String.replaceWhiteWithNewLine(): String =
            replace(replaceWhiteWithNewLineRegex, "\n")

    /**
     * 忽略单个元素处理异常，
     */
    protected inline fun <R : Any> Elements.mapIgnoreException(transform: (Element) -> R) =
            mapHandleException({ element, t ->
                logger.debug("解析元素[${element.tag()}]失败，", t)
            }, transform)

    /**
     * 捕获单个元素处理异常，处理后继续，
     */
    protected inline fun <R : Any> Elements.mapHandleException(
            exceptionHandler: (Element, Throwable) -> Unit,
            transform: (Element) -> R
    ) = mapNotNull {
        try {
            transform(it)
        } catch (e: Exception) {
            // 错误忽略，只打个debug级别的日志，
            exceptionHandler(it, e)
            null
        }
    }

    /**
     * 下面的封装关键在于分开必要和不必要的，是否捕获Exception,
     * 必要的解析失败时统一抛异常，
     * TODO: 改成query可以为空，默认就返回当前元素，
     */
    protected fun Element.requireElements(query: String, name: String = TAG_LIST): Elements = try {
        select(query)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected inline fun <T> Element.requireElements(query: String, name: String = TAG_LIST, block: (Elements) -> T): T = try {
        select(query).let(block)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected fun Element.requireElement(query: String, name: String = TAG_ELEMENT): Element = try {
        select(query).first()
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected inline fun <T> Element.requireElement(query: String, name: String = TAG_ELEMENT, block: (Element) -> T): T = try {
        select(query).first().let(block)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected fun Element.getElements(query: String): Elements? = try {
        select(query)
    } catch (e: Exception) {
        logger.debug("解析元素($query)失败，", e)
        null
    }

    protected inline fun <T> Element.getElements(query: String, block: (Elements) -> T?): T? = try {
        select(query).let(block)
    } catch (e: Exception) {
        logger.debug("解析元素($query)失败，", e)
        null
    }

    protected fun Element.getElement(query: String): Element? = try {
        select(query).first()
    } catch (e: Exception) {
        logger.debug("解析元素($query)失败，", e)
        null
    }

    protected inline fun <T> Element.getElement(query: String, block: (Element) -> T?): T? = try {
        select(query).first().let(block)
    } catch (e: Exception) {
        logger.debug("解析元素($query)失败，", e)
        null
    }

    /**
     * 返回通过pattern提取元素中的字符串的lambda,
     */
    protected fun pickString(pattern: String): (Element) -> String = {
        it.text().pick(compilePattern(pattern)).first()
    }

    /**
     * 用于获取简介，一个元素，简介内容都在ownText里，子标签可能有无用信息，
     */
    protected fun ownLinesString(): (Element) -> String = {
        it.ownTextList().joinToString("\n")
    }

    /**
     * 用于获取正文，一个元素，正文内容都在ownText里，子标签可能有无用信息，
     */
    protected fun ownLines(): (Element) -> List<String> = {
        it.ownTextList()
    }

    /**
     * URLEncode with gbk file encoding,
     */
    protected fun gbk(value: String): String = URLEncoder.encode(value, "GBK")

    protected fun utf8(value: String): String = URLEncoder.encode(value, "UTF-8")
}