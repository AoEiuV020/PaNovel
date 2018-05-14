package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.base.jar.trace
import cc.aoeiuv020.panovel.api.site.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * 小说网站的上下文，
 * Created by AoEiuV020 on 2017.10.02-15:25:48.
 */
@Suppress("MemberVisibilityCanPrivate")
abstract class NovelContext {
    companion object {
        private val gson: Gson = GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
        private var sCacheDir: File? = null
        fun cache(cacheDir: File?) {
            this.sCacheDir = cacheDir?.takeIf { (it.exists() && it.isDirectory) || it.mkdirs() }
        }

        @Suppress("RemoveExplicitTypeArguments")
        private val contexts: List<NovelContext> = listOf(
                Piaotian(), Biquge(), Liudatxt(), Qidian(), Dmzz(), Sfacg(), Snwx(), Syxs(),
                Yssm(), Qlyx()
        )
        private val hostMap = contexts.associateBy { URL(it.getNovelSite().baseUrl).host }
        private val nameMap = contexts.associateBy { it.getNovelSite().name }
        fun getNovelContexts(): List<NovelContext> = contexts
        fun getNovelContextByUrl(url: String): NovelContext {
            val host = URL(url).host
            return hostMap[host] ?: contexts.firstOrNull { it.check(url) }
            ?: throw IllegalArgumentException("网址不支持: $url")
        }

        fun getNovelContextBySite(site: NovelSite): NovelContext = getNovelContextByUrl(site.baseUrl)

        @Suppress("unused")
        fun getNovelContextByName(name: String): NovelContext {
            return nameMap[name] ?: throw IllegalArgumentException("网站不支持: $name")
        }
    }

    @Suppress("MemberVisibilityCanPrivate")
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    /**
     * 用类名simpleName当缓存目录名，所以类名不能重复，
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val mCacheDir: File?
        get() = sCacheDir?.resolve(this.javaClass.simpleName)?.apply { exists() || mkdirs() }
    private val mCookieFile: File? get() = mCacheDir?.resolve("cookies")
    private var _cookies: Map<String, String>? = null
    var cookies: Map<String, String>
        @Synchronized
        get() = _cookies ?: (mCookieFile?.let { file ->
            try {
                file.readText().toBean<MutableMap<String, String>>(gson)
            } catch (e: Exception) {
                file.delete()
                null
            }
        } ?: mutableMapOf()).also {
            _cookies = it
        }
        @Synchronized
        private set(value) {
            logger.debug {
                "setCookies $value"
            }
            if (value == _cookies) {
                return
            }
            _cookies = value
            mCookieFile?.writeText(value.toJson(gson))
        }

    fun putCookies(cookies: Map<String, String>) {
        if (cookies.isEmpty()) {
            return
        }
        // 要确保setCookies被调用才会本地保存cookie,
        this.cookies = this.cookies + cookies
    }

    fun removeCookies() {
        this.cookies = mapOf()
    }

    /**
     *
     */
    open fun cookieDomainList(): List<String> {
        val host = URL(getNovelSite().baseUrl).host
        val domain = secondLevelDomain(host)
        return listOf(domain)
    }

    private fun secondLevelDomain(host: String): String {
        val index1 = host.lastIndexOf('.')
        val index2 = host.lastIndexOf('.', index1 - 1)
        return host.substring(index2)
    }

    /**
     * 有的网站没有指定编码，只能在这里强行指定，
     * null表示用默认的，一版可以，
     */
    protected open val charset: String? = null

    abstract fun getNovelSite(): NovelSite
    /**
     * 获取网站分类信息，
     */
    open fun getGenres(): List<NovelGenre> = listOf()

    /**
     * 获取分类页面的下一页，
     */
    open fun getNextPage(genre: NovelGenre): NovelGenre? = null

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
    open fun searchNovelAuthor(author: String): NovelGenre = searchNovelName(author)

    /**
     * 获取小说详情页信息，
     */
    abstract fun getNovelDetail(requester: DetailRequester): NovelDetail

    open fun getNovelItem(url: String): NovelItem = getNovelDetail(DetailRequester(url)).novel

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
    open fun check(url: String): Boolean = try {
        secondLevelDomain(URL(getNovelSite().baseUrl).host) == secondLevelDomain(URL(url).host)
    } catch (_: Exception) {
        false
    }

    /**
     * 封装网络请求，主要是为了统一打log,
     * TODO: 整理下，最终得到的有document、string两种，是否设置cookies再分两种，
     */
    protected fun connect(requester: Requester): Connection {
        logger.trace {
            val stack = Thread.currentThread().stackTrace
            stack.drop(2).take(6).joinToString("\n", "stack trace\n") {
                "\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        }
        logger.debug { "request $requester" }
        return requester.connect()
    }

    protected fun connect(url: String) = connect(Requester(url))

    protected fun response(conn: Connection, doBeforeExecute: Connection.() -> Connection = { this }): Connection.Response {
        // 设置cookies,
        cookies.takeIf { it.isNotEmpty() }?.let { conn.cookies(it) }
        val response = conn.doBeforeExecute().execute()
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

    protected fun response(requester: Requester): Connection.Response = response(connect(requester), requester::doBeforeExecute)

    protected fun response(url: String) = response(Requester(url))

    protected fun request(response: Connection.Response): Document = response.parse()

    protected fun request(requester: Requester): Document = request(response(requester))

    protected fun request(url: String) = request(Requester(url))
}