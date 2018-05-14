package cc.aoeiuv020.panovel.api

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

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

    protected abstract val site: NovelSite

    override fun getNovelSite(): NovelSite = site

    protected fun Element.src(): String = attr("src")
    protected fun Element.absSrc(): String = absUrl("src")
    protected fun Element.href(): String = attr("href")
    protected fun Element.absHref(): String = absUrl("href")
    protected fun Element.title(): String = attr("title")
    protected fun Element.textList(): List<String> = childNodes().mapNotNull {
        (it as? TextNode)?.wholeText?.takeIf { it.isNotBlank() }?.trim()
    }

    private val replaceWhiteWithNewLineRegex = Regex("\\s+")
    protected fun String.replaceWhiteWithNewLine(): String =
            replace(replaceWhiteWithNewLineRegex, "\n")

    /**
     * 下面的封装关键在于分开必要和不必要的，是否捕获Exception,
     * 必要的解析失败时统一抛异常，
     */
    protected fun Element.requireElements(query: String, name: String = TAG_LIST): Elements = try {
        select(query)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]失败，", e)
    }

    protected fun <T> Element.requireElements(query: String, name: String = TAG_LIST, block: (Elements) -> T): T = try {
        select(query).let(block)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]失败，", e)
    }

    protected fun Element.requireElement(query: String, name: String = TAG_ELEMENT): Element = try {
        select(query).first()
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]失败，", e)
    }

    protected fun <T> Element.requireElement(query: String, name: String = TAG_ELEMENT, block: (Element) -> T): T = try {
        select(query).first().let(block)
    } catch (e: Exception) {
        throw IllegalStateException("解析[$name]失败，", e)
    }

    protected fun Element.getElement(query: String): Element? = try {
        select(query).firstOrNull()
    } catch (e: Exception) {
        null
    }

    protected fun <T> Element.getElement(query: String, block: (Element) -> T?): T? = try {
        select(query).firstOrNull()?.let(block)
    } catch (e: Exception) {
        null
    }
}