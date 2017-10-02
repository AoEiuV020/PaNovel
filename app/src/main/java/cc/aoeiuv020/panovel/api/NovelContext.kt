package cc.aoeiuv020.panovel.api

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL

/**
 * 小说网站的上下文，
 * Created by AoEiuV020 on 2017.10.02-15:25:48.
 */
abstract class NovelContext {
    companion object {
        @Suppress("RemoveExplicitTypeArguments")
        private val contexts: List<NovelContext> = listOf()
        private val contextsMap = contexts.associateBy { URL(it.getNovelSite().baseUrl).host }
        fun getNovelContexts(): List<NovelContext> = contexts
        fun getNovelContext(url: String): NovelContext? {
            val host: String
            try {
                host = URL(url).host
            } catch (_: Exception) {
                return null
            }
            return contextsMap[host] ?: contexts.firstOrNull { it.check(url) }
        }
    }

    @Suppress("MemberVisibilityCanPrivate")
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    abstract fun getNovelSite(): NovelSite
    /**
     * 获取网站分类信息，
     */
    abstract fun getGenres(): List<NovelGenre>

    /**
     * 获取分类页面的下一页，
     */
    abstract fun getNextPage(genre: NovelGenre): NovelGenre?

    /**
     * 获取分类页面里的小说列表信息，
     */
    abstract fun getNovelList(genre: NovelGenre): List<NovelListItem>

    /**
     * 搜索小说名，
     */
    abstract fun searchNovelName(name: String): NovelGenre

    /**
     * 搜索小说作者，
     */
    abstract fun searchNovelAuthor(author: String): NovelGenre

    abstract fun isSearchResult(genre: NovelGenre): Boolean

    /**
     * 获取小说详情页信息，
     */
    abstract fun getNovelDetail(novelDetailUrl: NovelDetailUrl): NovelDetail

    /**
     * 获取小说章节文本内容，
     */
    abstract fun getNovelText(novelChapter: NovelChapter): NovelText

    internal fun check(url: String): Boolean = URL(getNovelSite().baseUrl).host == URL(url).host


    protected fun get(url: String, parameters: Map<String, String> = emptyMap()): Document {
        logger.trace {
            val stack = Thread.currentThread().stackTrace
            stack.drop(2).take(6).joinToString("\n", "stack trace\n") {
                "\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        }
        logger.debug { "get $url" }
        val conn = Jsoup.connect(url)
        logger.debug { "parameters $parameters" }
        conn.data(parameters)
        // 网络连接失败直接抛出，
        val root = conn.get()
        logger.debug { "status code: ${conn.response().statusCode()}" }
        logger.debug { "response url: ${conn.response().url()}" }
        if (!check(conn.response().url().toString())) {
            throw IOException("网络被重定向，检查网络是否可用，")
        }
        return root
    }

    protected fun post(url: String, parameters: Map<String, String> = emptyMap()): Document {
        logger.trace {
            val stack = Thread.currentThread().stackTrace
            stack.drop(2).take(6).joinToString("\n", "stack trace\n") {
                "\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        }
        logger.debug { "post $url" }
        val conn = Jsoup.connect(url)
        logger.debug { "args $parameters" }
        conn.data(parameters)
        conn.postDataCharset(getNovelSite().charset)
        val root = conn.post()
        logger.debug { "status code: ${conn.response().statusCode()}" }
        logger.debug { "response url: ${conn.response().url()}" }
        if (!check(conn.response().url().toString())) {
            throw IOException("网络被重定向，检查网络是否可用，")
        }
        return root
    }
}