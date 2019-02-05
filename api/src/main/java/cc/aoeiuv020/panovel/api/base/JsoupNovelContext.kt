package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.ownTextList
import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.regex.pick
import okhttp3.Call
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder

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
        const val TAG_VOLUME = "分卷"
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

    }

    /**
     * 有的网站没有指定编码，只能在这里强行指定，
     * null表示用默认的，一般可以，
     * 有点混乱，一般在parse前指定，不用这里的，
     */
    protected open val charset: String? get() = null

    protected fun parse(extra: String, listener: ((Long, Long) -> Unit)? = null): Document = parse(connect(absUrl(extra)), listener = listener)
    protected fun parse(input: InputStream, charset: String?, baseUri: String): Document = try {
        Jsoup.parse(input, charset, baseUri)
    } catch (e: Exception) {
        if (!check(baseUri)) {
            // 解析失败再判断是响应地址是否正确，
            // 如果是网站改域名，虽然是check地址不会通过，但是解析是正常的，
            throw IOException("网络被重定向，检查网络是否可用， <$baseUri>，", e)
        } else {
            throw IllegalStateException("页面<$baseUri>解析失败，", e)
        }
    }

    protected fun parse(call: Call, charset: String? = this.charset, listener: ((Long, Long) -> Unit)? = null): Document {
        val response = response(call)
        // 用Jsoup解析okhttp得到的InputStream，
        // 编码如果指定，就用指定的，没有就从okhttp的response中拿，再没有传null, Jsoup会自己尝试解析，
        // 如果有301之类跳转，最终响应的地址作为Jsoup解析的baseUri, 主要应该只是从相对地址计算绝对地址时用到，
        return response.inputStream(listener) { input ->
            parse(
                    input,
                    charset ?: response.charset(),
                    response.url()
            )
        }
    }

    /**
     * 下一页相关的暂不支持，
     */
    override fun getNextPage(extra: String): String? = getNextPage(parse(extra))

    protected open fun getNextPage(root: Document): String? = null


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
        select(query).notEmpty(query)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected inline fun <T> Element.requireElements(query: String, name: String = TAG_LIST, block: (Elements) -> T): T = try {
        select(query).notEmpty(query).let(block)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected fun Element.requireElement(query: String, name: String = TAG_ELEMENT): Element = try {
        select(query).first().notNull(query)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected inline fun <T> Element.requireElement(query: String, name: String = TAG_ELEMENT, block: (Element) -> T): T = try {
        select(query).first().notNull(query).let(block)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]($query)失败，", e)
    }

    protected fun Elements.notEmpty(query: String = ""): Elements = apply {
        if (isEmpty()) {
            throw IllegalStateException("解析元素${query}结果为空，")
        }
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
        it.text().pick(pattern).first()
    }

    /**
     * 用于获取简介，一个元素，简介内容都在ownText里，子标签可能有无用信息，
     */
    protected fun ownLinesString(): (Element) -> String = {
        it.ownTextListSplitWhitespace().joinToString("\n")
    }

    protected fun ownText(): (Element) -> String = {
        it.ownText().trim()
    }

    /**
     * 用于获取正文，一个元素，正文内容都在ownText里，子标签可能有无用信息，
     */
    protected fun ownLinesSplitWhitespace(): (Element) -> List<String> = {
        it.ownTextListSplitWhitespace()
    }

    protected fun ownLines(): (Element) -> List<String> = {
        it.ownTextList()
    }

    /**
     * URLEncode with gbk file encoding,
     */
    protected fun gbk(value: String): String = URLEncoder.encode(value, "GBK")

    protected fun utf8(value: String): String = URLEncoder.encode(value, "UTF-8")
}