package cc.aoeiuv020.panovel.api

import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL

/**
 * 小说网站的上下文，
 * Created by AoEiuV020 on 2017.10.02-15:25:48.
 */
@Suppress("MemberVisibilityCanPrivate")
abstract class NovelContext {
    companion object {
        @Suppress("RemoveExplicitTypeArguments")
        private val contexts: List<NovelContext> = listOf(Piaotian())
        private val contextsMap = contexts.associateBy { URL(it.getNovelSite().baseUrl).host }
        fun getNovelContexts(): List<NovelContext> = contexts
        fun getNovelContext(url: String): NovelContext {
            val host: String
            try {
                host = URL(url).host
            } catch (e: Exception) {
                throw e
            }
            return contextsMap[host] ?: contexts.firstOrNull { it.check(url) } ?: throw IllegalArgumentException("网址不支持: $url")
        }

        fun getNovelContext(site: NovelSite): NovelContext = getNovelContext(site.baseUrl)
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
    abstract fun getNovelList(requester: ListRequester): List<NovelListItem>

    /**
     * 搜索小说名，
     */
    abstract fun searchNovelName(name: String): NovelGenre

    /**
     * 搜索小说作者，
     */
    abstract fun searchNovelAuthor(author: String): NovelGenre

    /**
     * 判断这个对象是不是搜索产生的，
     */
    abstract fun isSearchResult(genre: NovelGenre): Boolean

    /**
     * 获取小说详情页信息，
     */
    abstract fun getNovelDetail(requester: DetailRequester): NovelDetail

    /**
     * 获取小说章节列表，
     */
    abstract fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter>

    /**
     * 获取小说章节文本内容，
     */
    abstract fun getNovelText(requester: TextRequester): NovelText

    /**
     * 判断这个地址是不是属于这个网站，
     */
    internal fun check(url: String): Boolean = URL(getNovelSite().baseUrl).host == URL(url).host

    /**
     * 封装网络请求，主要是为了统一打log,
     */
    protected fun response(requester: Requester): Connection.Response {
        logger.trace {
            val stack = Thread.currentThread().stackTrace
            stack.drop(2).take(6).joinToString("\n", "stack trace\n") {
                "\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        }
        logger.debug { "request $requester" }
        return requester.request()
    }

    protected fun request(response: Connection.Response): Document {
        val root = response.parse()
        logger.debug { "status code: ${response.statusCode()}" }
        logger.debug { "response url: ${response.url()}" }
        if (!check(response.url().toString())) {
            throw IOException("网络被重定向，检查网络是否可用，")
        }
        return root
    }

    protected fun request(requester: Requester): Document = request(response(requester))

    protected fun request(url: String) = request(Requester(url))
}