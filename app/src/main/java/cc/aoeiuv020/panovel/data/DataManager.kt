package cc.aoeiuv020.panovel.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.data.entity.*
import cc.aoeiuv020.panovel.local.ImportRequireValue
import cc.aoeiuv020.panovel.server.dal.model.QueryResponse
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.util.notNullOrReport
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel as ServerNovel

/**
 * 封装多个数据库的联用，
 * 隐藏api模块的数据类，app只使用这里的数据库实体，
 *
 * Created by AoEiuV020 on 2018.04.28-16:53:14.
 */
object DataManager : AnkoLogger {
    lateinit var app: AppDatabaseManager
    lateinit var api: ApiManager
    lateinit var cookie: CookieManager
    lateinit var cache: CacheManager
    lateinit var server: ServerManager
    @SuppressLint("StaticFieldLeak")
    lateinit var local: LocalManager
    @SuppressLint("StaticFieldLeak")
    lateinit var download: DownloadManager

    @Synchronized
    fun init(ctx: Context) {
        if (!::app.isInitialized) {
            app = AppDatabaseManager(ctx)
        }
        if (!::api.isInitialized) {
            api = ApiManager(ctx)
        }
        if (!::cookie.isInitialized) {
            cookie = CookieManager(ctx)
        }
        if (!::cache.isInitialized) {
            cache = CacheManager(ctx)
        }
        if (!::server.isInitialized) {
            server = ServerManager(ctx)
        }
        if (!::local.isInitialized) {
            local = LocalManager(ctx)
        }
        if (!::download.isInitialized) {
            download = DownloadManager(ctx)
        }
    }

    fun resetCacheLocation(ctx: Context) {
        cache.resetCacheLocation(ctx)
    }

    fun listBookshelf(): List<NovelManager> = app.listBookshelf(ListSettings.bookshelfOrderBy).map { it.toManager() }

    /**
     * 收到更新推送, 主动更新一下看看是不是真的有更新，
     *
     * @return 返回小说对象，以及是否真的刷出了更新，
     */
    fun receiveUpdate(novel: ServerNovel): Pair<Novel, Boolean> {
        val localNovel = app.queryOrNewNovel(NovelMinimal(novel))
        return localNovel to if (localNovel.chaptersCount < novel.chaptersCount) {
            // 章节数更多了表示确实有更新，
            // 不能太相信推送的数据，一切以本地自己刷新的为准，
            val oldCount = localNovel.chaptersCount
            localNovel.toManager().requestChapters(true)
            val newCount = localNovel.chaptersCount
            newCount > oldCount
        } else {
            false
        }
    }

    fun getNovelManager(id: Long): NovelManager =
            app.query(id).toManager()

    private fun Novel.toManager() = if (site.startsWith(".")) {
        // 网站名是点.开头的表示本地小说，让LocalManager提供数据，
        NovelManager(this, app, local.getNovelProvider(this), cache, null, download.dnmLocal)
    } else {
        NovelManager(this, app, api.getNovelProvider(this), cache, server, download.dnmLocal)
    }

    fun allNovelContexts() = api.contexts

    /**
     * 列出所有网站，
     */
    fun listSites(): List<Site> = app.db.siteDao().list()

    /**
     * 同步所有网站到数据库，app升级时调用一次就好，
     */
    fun syncSites(): Unit = app.db.runInTransaction<Unit> {
        val existsSites = app.db.siteDao().listAllSite()
        existsSites.forEach { site ->
            val context = try {
                api.getNovelContextByName(site.name)
            } catch (e: Exception) {
                app.db.siteDao().removeSite(site)
                return@forEach
            }
            var dirty = false
            if (site.hide != context.hide) {
                site.hide = context.hide
                dirty = true
            }
            if (site.baseUrl != context.site.baseUrl) {
                site.baseUrl = context.site.baseUrl
                dirty = true
            }
            if (site.logo != context.site.logo) {
                site.logo = context.site.logo
                dirty = true
            }
            // 如果有变化，就更新该字段，
            if (dirty) {
                app.db.siteDao().updateSite(site)
            }
        }
        // 新增的网站加入网站表，
        val existsSiteNameSet = existsSites.map { it.name }.toSet()
        allNovelContexts().filter { it.site.name !in existsSiteNameSet }.forEach { context ->
            context.site.run {
                app.newSite(name, baseUrl, logo, context.enabled, context.hide)
            }
        }
    }

    /**
     * @param author 作者名为空就不从数据库查询，
     */
    fun search(site: String, name: String, author: String?): List<NovelManager> {
        if (author != null) {
            // 如果有作者名，那结果只可能有一个，
            // 如果数据库里有了，就直接返回，
            app.query(site, author, name)?.also {
                return listOf(it.toManager())
            }
        }
        val context = api.getNovelContextByName(site)
        val resultList = api.search(context, name)
        return app.db.runInTransaction<List<NovelManager>> {
            resultList.map {
                // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
                app.queryOrNewNovel(NovelMinimal(it)).toManager()
            }
        }
    }

    fun getNovelContextByName(site: String) = api.getNovelContextByName(site)

    @MainThread
    fun pushCookiesToWebView(context: NovelContext) {
        // 高版本的设置cookie的回调乱七八糟的，用不上，
        context.cookies.values.forEach { okhttpCookie ->
            val cookieString = okhttpCookie.toString()
            debug { "push cookie: <$cookieString>" }
            // webView传入cookie一次只能一条，取出一次所有，
            // cookieString只有一条cookie, 可能包含domain, path之类，分号;分隔，webView这个可以识别，
            cookie.putCookie(context.site.baseUrl, cookieString)
        }
    }

    @WorkerThread
    fun syncCookies(ctx: Context?) = cookie.sync(ctx)

    /**
     * TODO: 这里有NovelContext拿到cookies后的文件操作，
     * 但是WebView cookies操作好像只能在主线程，
     * 索性都放主线程吧，不是很费时，
     */
    @MainThread
    fun pullCookiesFromWebView(context: NovelContext) {
        val httpUrl = HttpUrl.parse(context.site.baseUrl).notNullOrReport()
        // webView传入cookie一次只能一条，取出一次所有，
        cookie.getCookies(context.site.baseUrl)?.split(";")?.mapNotNull { cookiePair ->
            debug { "pull cookie: <$cookiePair>" }
            // 取出来的cookiePair只有name=value，Cookie.parse一定能通过，也因此可能有超时信息拿不出来的问题，
            Cookie.parse(httpUrl, cookiePair)?.let { cookie ->
                cookie.name() to cookie
            }
        }?.let { cookiesList ->
            context.putCookies(cookiesList.toMap())
        }
    }

    fun getNovelFromUrl(site: String, url: String): NovelManager {
        return api.getNovelFromUrl(getNovelContextByName(site), url).let {
            // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
            app.queryOrNewNovel(NovelMinimal(it)).toManager()
        }
    }

    fun removeWebViewCookies() = cookie.removeCookies()

    fun removeNovelContextCookies(site: String) = api.removeCookies(getNovelContextByName(site))
    fun pinned(site: Site) {
        site.pinnedTime = Date()
        app.updatePinnedTime(site)
    }

    fun cancelPinned(site: Site) {
        site.pinnedTime = Date(0)
        app.updatePinnedTime(site)
    }

    fun updateReadStatus(novel: Novel) = app.updateReadStatus(novel)

    /**
     * @return 返回该小说已经缓存的章节列表，
     * 考虑到缓存可能对key进行了单向加密，
     * 只用于contains判断特定章节是否已经缓存，不用于读取章节信息，
     */
    fun novelContentsCached(novel: Novel): Collection<String> = cache.novelContentCached(novel)

    fun siteEnabledChange(site: Site) = app.siteEnabledChange(site)

    fun history(historyCount: Int): List<NovelManager> = app.history(historyCount).map { it.toManager() }

    fun getBookList(bookListId: Long): BookList = app.getBookList(bookListId)

    /**
     * 列表中的小说在该书单里的包含情况，
     */
    fun inBookList(bookListId: Long, list: List<NovelManager>) =
    // 多费一个map,
            app.inBookList(bookListId, list.map { it.novel })

    fun getNovelFromBookList(bookListId: Long): List<Novel> = app.getNovelFromBookList(bookListId)
    fun getNovelManagerFromBookList(bookListId: Long): List<NovelManager> =
            getNovelFromBookList(bookListId).map { it.toManager() }

    // 不包括本地小说，
    fun getNovelMinimalFromBookList(bookListId: Long): List<NovelMinimal> = app.getNovelMinimalFromBookList(bookListId)

    fun allBookList() = app.allBookList()
    fun renameBookList(bookList: BookList, name: String) = app.renameBookList(bookList, name)
    fun copyBookList(bookList: BookList, name: String) = app.copyBookList(bookList, name)
    fun removeBookList(bookList: BookList) = app.removeBookList(bookList)
    fun newBookList(name: String) = app.newBookList(name)

    /**
     * @throws IllegalArgumentException 不支持的地址直接抛异常，
     */
    fun getNovelFromUrl(url: String): Novel = api.getNovelFromUrl(api.getNovelContextByUrl(url), url).let {
        // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
        app.queryOrNewNovel(NovelMinimal(it))
    }

    fun importBookList(name: String, list: List<NovelMinimal>, uuid: String = UUID.randomUUID().toString()) =
            app.importBookList(name, list, uuid)

    fun addToBookshelf(bookList: BookList) {
        app.addBookshelf(bookList)
        // 向极光订阅对应tag,
        server.addTags(getNovelFromBookList(bookList.nId))
    }

    fun removeFromBookshelf(bookList: BookList) {
        app.removeBookshelf(bookList)
        // 向极光取消订阅对应tag,
        server.removeTags(getNovelFromBookList(bookList.nId))
    }

    /**
     * 小说导入书架，包含进度，
     */
    fun importBookshelfWithProgress(list: List<NovelWithProgress>) = app.db.runInTransaction {
        debug { "$list" }
        val novelList = list.mapNotNull {
            // 查询或插入，得到小说对象，再更新进度，
            val novel = app.queryOrNewNovel(NovelMinimal(it))
            if (!app.checkSiteSupport(novel)) {
                // 网站不在支持列表就不添加，
                // 基本信息已经写入数据库也无所谓了，
                return@mapNotNull null
            }
            novel.readAtChapterIndex = it.readAtChapterIndex
            novel.readAtTextIndex = it.readAtTextIndex
            // 顺便更新下阅读至的章节名，
            if (novel.chapters != null) {
                novel.readAtChapterName = cache.loadChapters(novel)?.getOrNull(novel.readAtChapterIndex)?.name ?: ""
            }
            // 加入书架，
            novel.bookshelf = true
            // 不调用方法updateBookshelf，因为这个方法包含订阅更新推送，
            app.updateBookshelf(novel)
            // 普通更新阅读进度，比起来少了阅读时间，无所谓了，
            updateReadStatus(novel)
            novel
        }
        // 向极光订阅/取消对应tag,
        server.addTags(novelList)
    }

    /**
     * 小说导入书架，不包含进度，
     */
    fun importBookshelf(list: List<NovelMinimal>) = app.db.runInTransaction {
        debug { "$list" }
        val novelList = list.mapNotNull {
            // 查询或插入，得到小说对象，再更新进度，
            val novel = app.queryOrNewNovel(it)
            if (!app.checkSiteSupport(novel)) {
                // 网站不在支持列表就不添加，
                // 基本信息已经写入数据库也无所谓了，
                return@mapNotNull null
            }
            // 顺便更新下阅读至的章节名，
            if (novel.chapters != null) {
                novel.readAtChapterName = cache.loadChapters(novel)?.getOrNull(novel.readAtChapterIndex)?.name ?: ""
            }
            // 加入书架，
            novel.bookshelf = true
            // 不调用方法updateBookshelf，因为这个方法包含订阅更新推送，
            app.updateBookshelf(novel)
            // 普通更新阅读进度，比起来少了阅读时间，无所谓了，
            updateReadStatus(novel)
            novel
        }
        // 向极光订阅/取消对应tag,
        server.addTags(novelList)
    }

    /**
     * 小说导入进度，
     */
    fun importNovelWithProgress(list: Sequence<NovelWithProgressAndPinnedTime>): Int = app.db.runInTransaction<Int> {
        debug { "$list" }
        var count = 0
        list.forEach {
            // 查询或插入，得到小说对象，再更新进度，
            val novel = app.queryOrNewNovel(NovelMinimal(it))
            if (!app.checkSiteSupport(novel)) {
                // 网站不在支持列表就不添加，
                // 基本信息已经写入数据库也无所谓了，
                return@forEach
            }
            novel.readAtChapterIndex = it.readAtChapterIndex
            novel.readAtTextIndex = it.readAtTextIndex
            novel.pinnedTime = it.pinnedTime
            // 顺便更新下阅读至的章节名，
            if (novel.chapters != null) {
                novel.readAtChapterName = cache.loadChapters(novel)?.getOrNull(novel.readAtChapterIndex)?.name ?: ""
            }
            // 普通更新阅读进度，比起来少了阅读时间，无所谓了，
            updateReadStatus(novel)
            ++count
        }
        count
    }

    fun cleanAllCache() {
        cache.cleanAll()
        api.cleanCache()
    }

    fun cleanBookshelf() = app.cleanBookshelf()

    fun cleanBookList() = app.cleanBookList()

    fun cleanHistory() = app.cleanHistory()

    /**
     * 重置书架订阅情况，覆盖此前的所有tags,
     * 向极光订阅书架上的小说，
     * 只能异步，所以传入回调，
     * 回调是收到极光的广播时调用，在ui线程的，
     */
    fun resetSubscript() =
            server.setTags(app.listBookshelf(ListSettings.bookshelfOrderBy))

    fun removeAllCookies() {
        removeWebViewCookies()
        syncCookies(App.ctx)
        listSites().forEach {
            removeNovelContextCookies(it.name)
        }
    }

    /**
     * 返回书架上有更新的小说列表，
     * 按收到更新的时间倒序排列，
     */
    fun hasUpdateNovelList(): List<Novel> = app.hasUpdateNovelList()

    /**
     * @param requestInput 有的需要让用户输入决定，比如编码，作者名，小说名，还有文件类型，
     */
    @WorkerThread
    fun importLocalNovel(ctx: Context, uri: Uri, requestInput: (ImportRequireValue, String) -> String?): Novel {
        val (novel, chapterList) = ctx.contentResolver.openInputStream(uri).notNullOrReport(uri.toString()).use { input ->
            local.importLocalNovel(input, uri.toString(), requestInput)
        }
        app.queryOrNewNovel(NovelMinimal(novel)).let { exists ->
            // 如果同bookId的小说已经存在，就覆盖全部字段，
            // 只需要保留一个id,
            novel.id = exists.id
            app.updateAll(novel)
        }
        // 导入时已经解析了一遍章节列表，缓存起来，不能白解析了，
        // 统一转换成api模块的章节格式再存，
        cache.saveChapters(novel, chapterList)
        debug {
            "importLocalNovel result: $novel"
        }
        return novel
    }

    /**
     * 返回有更新的小说的id,
     */
    fun askUpdate(list: List<NovelManager>): List<Long> {
        debug { "askUpdate ${list.map { it.novel.bookId }}" }
        // 用无视服务器端返回的顺序，方便改成只返回部分数据，
        // 连接不上服务器时返回emptyMap，
        val resultMap: Map<Long, QueryResponse> = server.askUpdate(list.mapNotNull {
            // 本地小说不询问更新，
            if (it.novel.isLocalNovel) null else it.novel
        })
        return list.mapNotNull {
            val novel = it.novel
            // 不在返回map里的无视，如果连接不上服务器就全都无视，
            val result = resultMap[novel.nId] ?: return@mapNotNull null
            if (result.chaptersCount > novel.chaptersCount) {
                // 只对比章节数，如果更大就是有更新，返回，最后通知刷新列表，开始刷新小说，
                debug { "${novel.bookId} has update ${result.chaptersCount} > ${novel.chaptersCount}" }
                novel.nId
            } else {
                debug { "${novel.bookId} no update ${result.chaptersCount} <= ${novel.chaptersCount}" }
                // 如果没更新，就保存服务器上的更新时间，如果更大的话，
                novel.apply {
                    // 不更新receiveUpdateTime，不准，有时别人比较晚收到同一个更新然后推上去被拿到，
                    checkUpdateTime = maxOf(checkUpdateTime, result.checkUpdateTime)
                }
                null
            }
        }
    }

    @WorkerThread
    fun downloadAll() {
        debug { "downloadAll called" }
        download.downloadAll(listBookshelf())
    }

    fun exportNovelProgress(): List<Novel> {
        return app.exportNovelProgress()
    }

}
