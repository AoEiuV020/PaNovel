package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.api.site.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern

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
        /**
         * 缓存文件夹，可以随时被删除的，
         */
        private var sCacheDir: File? = null

        fun cache(cacheDir: File?) {
            this.sCacheDir = cacheDir?.takeIf { (it.exists() && it.isDirectory) || it.mkdirs() }
        }

        /**
         * 非缓存，不会随时被删除，不要放太大的东西，
         */
        private var sFilesDir: File? = null

        fun files(filesDir: File?) {
            this.sFilesDir = filesDir?.takeIf { (it.exists() && it.isDirectory) || it.mkdirs() }
        }

        @Suppress("RemoveExplicitTypeArguments")
        private val contexts: List<NovelContext> by lazy {
            listOf(
                    Piaotian(), Biquge(), Liudatxt(), Qidian(), Dmzz(), Sfacg(), Snwx(), Syxs(),
                    Yssm(), Qlyx()
            )
        }
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
     * 用网站名当数据保存目录名，网站名不能重复，
     */
    private val fileName get() = getNovelSite().name
    /**
     * 缓存文件夹，可以随时被删除的，
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val mCacheDir: File?
        get() = sCacheDir?.resolve(fileName)?.apply { exists() || mkdirs() }
    /**
     * 非缓存，不会随时被删除，不要放太大的东西，
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val mFilesDir: File?
        get() = sFilesDir?.resolve(fileName)?.apply { exists() || mkdirs() }

    private val cookiesFile: File? get() = mFilesDir?.resolve("cookies")
    private var _cookies: Map<String, String>? = null
    var cookies: Map<String, String>
        @Synchronized
        get() = _cookies ?: (cookiesFile?.let { file ->
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
            cookiesFile?.writeText(value.toJson(gson))
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

    abstract fun getNovelSite(): NovelSite

    /**
     * 获取搜索页面的下一页，
     * TODO: 考虑在搜索结果直接加上关于下一页的，或者干脆不要下一页吧，反正现在搜索结果和下一页分别获取解析页面挺浪费的，
     *
     */
    open fun getNextPage(extra: String): String? = null

    /**
     * 搜索小说名，
     *
     */
    abstract fun searchNovelName(name: String): List<NovelItem>

    /**
     * 获取小说详情页信息，
     *
     * @param extra [NovelItem.extra]
     */
    abstract fun getNovelDetail(extra: String): NovelDetail

    /**
     * 从给定地址找到这本小说，
     * 要尽可能支持，
     */
    open fun getNovelItem(url: String): NovelItem = getNovelDetail(url).novel

    /**
     * 获取小说章节列表，
     *
     * @param extra [NovelDetail.extra]
     */
    abstract fun getNovelChaptersAsc(extra: String): List<NovelChapter>

    /**
     * 获取小说章节文本内容，
     *
     * @param extra [NovelChapter.extra]
     */
    abstract fun getNovelText(extra: String): NovelText

    /**
     * 判断这个地址是不是属于这个网站，
     */
    open fun check(url: String): Boolean = try {
        secondLevelDomain(URL(getNovelSite().baseUrl).host) == secondLevelDomain(URL(url).host)
    } catch (_: Exception) {
        false
    }

    /**
     * 以下几个都是为了得到真实地址，毕竟extra现在支持各种随便写法，
     */
    open fun getNovelDetailUrl(extra: String): String =
            absUrl(detailTemplate?.format(findBookId(extra)) ?: extra)

    /**
     * 详情页地址模板，String.format形式，"/book/%s/"
     */
    protected open val detailTemplate: String? get() = null

    protected open fun getNovelChapterUrl(extra: String): String =
            absUrl(chapterTemplate?.format(findBookId(extra)) ?: extra)

    /**
     * 目录页地址模板，String.format形式，"/book/%s/"
     */
    protected open val chapterTemplate: String? get() = detailTemplate

    open fun getNovelContentUrl(extra: String): String =
            absUrl(contentTemplate?.format(findChapterId(extra)) ?: extra)

    /**
     * 正文页地址模板，String.format形式，"/book/%s/"
     */
    protected open val contentTemplate: String? get() = null

    /**
     * 尝试从extra获取真实地址，
     * 只支持extra本身就是完整地址和extra为斜杆/开头的file两种情况，
     */
    protected fun absUrl(extra: String): String = try {
        // 先尝试extra是否是个合法的地址
        // 好像没必要，URL会直接判断，
        URL(extra)
    } catch (e: MalformedURLException) {
        // 再尝试extra当成spec部分，拼接上网站baseUrl,
        URL(URL(getNovelSite().baseUrl), extra)
    }.toExternalForm()

    /**
     * 有继承给定正则就用上，没有找到就直接返回传入的数据，可能已经是bookId了，
     */
    protected open fun findBookId(extra: String): String = try {
        extra.pick(bookIdRegex).first()
    } catch (e: Exception) {
        extra
    }

    protected open val bookIdRegex: Pattern get() = firstIntPattern

    /**
     * 查找章节id, 是包括小说id的，
     * 有继承给定正则就用上，没有找到就直接返回传入的数据，可能已经是chapterId了，
     */
    protected open fun findChapterId(extra: String): String = try {
        extra.pick(chapterIdRegex).first()
    } catch (e: Exception) {
        extra
    }

    protected open val chapterIdRegex: Pattern get() = firstTwoIntPattern
}