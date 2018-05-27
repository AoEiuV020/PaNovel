package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.base.jar.compilePattern
import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.base.jar.trace
import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import java.io.IOException
import java.net.URLEncoder
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.10-18:08:08.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class JsoupNovelContext : NovelContext() {
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
         * 并不得到子元素里的文字，
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
     * null表示用默认的，一版可以，
     */
    protected open val charset: String? get() = null

    protected fun parse(extra: String): Document = parse(connect(absUrl(extra)))
    protected fun parse(conn: Connection, charset: String? = this.charset): Document = try {
        requireNotNull(response(conn, charset).parse())
    } catch (e: IOException) {
        // IOException保持IOException, 使用的时候统一把IOException当成网络错误，
        throw IOException("网络连接错误，", e)
    } catch (e: Exception) {
        throw IllegalStateException("页面<${conn.request().url()}>解析失败，", e)
    }

    /**
     * 下面一对一对，参数String的如果被继承，就可能用不到参数Document的方法，但是也要继承，
     */

    /**
     * 搜索，
     * 要继承[connectByNovelName]指定搜索请求方式，
     * 如果继承了[searchNovelName], 可以不继承[getSearchResultList]解析搜索页面，
     */
    protected open fun getSearchResultList(root: Document): List<NovelItem> = listOf()

    override fun searchNovelName(name: String): List<NovelItem> = getSearchResultList(parse(connectByNovelName(name)))
    protected open fun connectByNovelName(name: String): Connection = throw NotImplementedError()

    override fun getNextPage(extra: String): String? = getNextPage(parse(extra))
    protected open fun getNextPage(root: Document): String? = null

    override fun getNovelDetail(extra: String): NovelDetail = getNovelDetail(parse(getNovelDetailUrl(extra)))
    protected open fun getNovelDetail(root: Document): NovelDetail = throw NotImplementedError()

    override fun getNovelChaptersAsc(extra: String): List<NovelChapter> = getNovelChaptersAsc(parse(getNovelChapterUrl(extra)))
    protected open fun getNovelChaptersAsc(root: Document): List<NovelChapter> = throw NotImplementedError()

    override fun getNovelContent(extra: String): List<String> = getNovelText(parse(getNovelContentUrl(extra)))
    protected open fun getNovelText(root: Document): List<String> = throw NotImplementedError()

    /**
     * 封装网络请求，主要是为了统一打log,
     */
    protected fun connect(url: String): Connection {
        logger.trace {
            val stack = Thread.currentThread().stackTrace
            stack.drop(2).take(6).joinToString("\n", "stack trace\n") {
                "\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        }
        logger.debug { "request $url" }
        return Jsoup.connect(url).maxBodySize(0).also { conn ->
            // 设置cookies,
            cookies.takeIf { it.isNotEmpty() }?.let { conn.cookies(it) }
        }
    }

    protected fun response(conn: Connection, charset: String? = this.charset): Connection.Response {
        val response = conn.execute()
        // 指定编码，如果存在，
        charset?.let { response.charset(it) }
        // 保存cookies, 按条目覆盖，
        putCookies(response.cookies())
        logger.debug { "status code: ${response.statusCode()}" }
        logger.debug { "response url: ${response.url()}" }
        logger.trace { "body length: ${response.body().length}" }
        if (!check(response.url().toString())) {
            throw IOException("网络被重定向，检查网络是否可用，")
        }
        return response
    }


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
     * URLEncode with gbk file encoding,
     */
    protected fun gbk(value: String): String = URLEncoder.encode(value, "GBK")
}