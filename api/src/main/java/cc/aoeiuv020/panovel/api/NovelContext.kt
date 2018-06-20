package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cookie
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.MalformedURLException
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
        /**
         * 缓存文件夹，可以随时被删除的，
         */
        private var sCacheDir: File? = null

        fun cache(cacheDir: File?) {
            this.sCacheDir = cacheDir?.takeIf { (it.exists() && it.isDirectory) || it.mkdirs() }
        }

        fun cleanCache() {
            sCacheDir?.deleteRecursively()
        }

        /**
         * 非缓存，不会随时被删除，不要放太大的东西，
         */
        private var sFilesDir: File? = null

        fun files(filesDir: File?) {
            this.sFilesDir = filesDir?.takeIf { (it.exists() && it.isDirectory) || it.mkdirs() }
        }

    }

    @Suppress("MemberVisibilityCanPrivate")
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    /**
     * 用网站名当数据保存目录名，网站名不能重复，
     */
    private val fileName get() = site.name
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

    private val cookiesFile: File?
        get() = mFilesDir?.resolve("cookies")
    private var _cookies: Map<String, Cookie>? = null
    // name to Cookie,
    // 虽然浏览器支持相同name的不同cookie，按domain和path区分，但是这里一个context只有一个网站，不会有多个name,
    var cookies: Map<String, Cookie>
        @Synchronized
        get() = _cookies ?: (cookiesFile?.let { file ->
            try {
                file.readText().toBean<Map<String, Cookie>>(gson)
            } catch (e: Exception) {
                // 读取失败说明文件损坏，直接删除，下次保存，
                file.delete()
                null
            }
        } ?: mapOf()).also {
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

    /**
     * 保存okhttp得到的cookie， 不过滤，
     */
    fun putCookies(cookies: List<Cookie>) {
        this.cookies = this.cookies + cookies.map {
            it.name() to it
        }
    }

    /**
     * 保存webView拿到的cookie, 由于只有name=value信息，没有超时之类的，
     * 过滤一下，value一样的就没必要更新了，
     */
    fun putCookies(cookies: Map<String, Cookie>) {
        if (cookies.isEmpty()) {
            return
        }
        // 要确保setCookies被调用才会本地保存cookie,
        this.cookies = this.cookies + cookies.filter { (name, cookie) ->
            // 只更新value不同的，以免覆盖了okhttp拿到的包含超时等完整信息的cookie,
            this.cookies[name]?.value() != cookie.value()
        }
    }

    fun removeCookies() {
        // 只要赋值了就会覆盖本地保存的，
        this.cookies = mapOf()
    }

    /**
     *
     */
    open fun cookieDomainList(): List<String> {
        val host = URL(site.baseUrl).host
        val domain = secondLevelDomain(host)
        return listOf(domain)
    }

    protected fun secondLevelDomain(host: String): String {
        val index1 = host.lastIndexOf('.')
        val index2 = host.lastIndexOf('.', index1 - 1)
        return if (index2 == -1) {
            // 可能没有第二个点.也就是已经是个二级域名, index2会为-1,
            host
        } else {
            host.substring(maxOf(index2, 0))
        }
    }


    /**
     * 这个网站是否启用，
     */
    open val enabled: Boolean = true

    abstract val site: NovelSite

    /**
     * 打开网站的话，可能不希望打开根目录，那就重写这个变量，
     */
    val homePage: String get() = site.baseUrl

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
     * @param extra [NovelItem.extra] 尽量直接让这个extra就是bookId,
     */
    abstract fun getNovelDetail(extra: String): NovelDetail

    /**
     * 从给定地址找到这本小说，
     * 要尽可能支持，
     */
    abstract fun getNovelItem(url: String): NovelItem

    /**
     * 获取小说章节列表，
     *
     * @param extra [NovelDetail.extra]
     */
    abstract fun getNovelChaptersAsc(extra: String): List<NovelChapter>

    /**
     * 获取小说章节文本内容，
     * 正文段不包括开头的空格，
     *
     * @param extra [NovelChapter.extra]
     */
    abstract fun getNovelContent(extra: String): List<String>

    /**
     * 判断这个地址是不是属于这个网站，
     * 默认对比二级域名，正常情况所有三级域名都是一个同一个网站同一个NovelContext的，
     * 有的网站可能用到站外地址，比如使用了百度搜索，不能单纯缓存host,
     */
    open fun check(url: String): Boolean = try {
        secondLevelDomain(URL(site.baseUrl).host) == secondLevelDomain(URL(url).host)
    } catch (_: Exception) {
        false
    }

    /**
     * 以下几个都是为了得到真实地址，毕竟extra现在支持各种随便写法，
     * 要快，要能在ui线程使用，不能有网络请求，
     */
    abstract fun getNovelDetailUrl(extra: String): String

    abstract fun getNovelContentUrl(extra: String): String

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
        URL(URL(site.baseUrl), extra)
    }.toExternalForm()

    /**
     * 从extra中获取图片URL, 正常直接就是完整路径，
     */
    open fun getImage(extra: String): URL = URL(URL(site.baseUrl), extra)

}