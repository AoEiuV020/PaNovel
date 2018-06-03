package cc.aoeiuv020.panovel.data

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.data.entity.*
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.notNullOrReport
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
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
    @SuppressLint("StaticFieldLeak")
    lateinit var cookie: CookieManager
    lateinit var cache: CacheManager
    lateinit var server: ServerManager
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
    }

    fun listBookshelf(): List<Novel> = app.listBookshelf()

    fun updateBookshelf(novel: Novel) {
        app.updateBookshelf(novel.nId, novel.bookshelf)
        // 向极光订阅/取消对应tag,
        if (novel.bookshelf) {
            server.addTags(listOf(novel))
        } else {
            server.removeTags(listOf(novel))
        }
    }

    fun refreshChapters(novel: Novel): List<NovelChapter> {
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        val list = api.requestNovelChapters(novel)
        if (novel.readAtChapterName.isBlank()) {
            // 如果数据库中没有阅读进度章节，说明没阅读过，直接存第一章名字，
            // 也可能是导入的进度，所以不能直接写0, 要用readAtChapterIndex，
            novel.readAtChapterName = list.getOrNull(novel.readAtChapterIndex)?.name ?: ""
        }
        // 不管是否真的有更新，都更新数据库，至少checkUpdateTime是必须要更新的，
        app.updateChapters(
                novel.nId, novel.chaptersCount,
                novel.readAtChapterName, novel.lastChapterName,
                novel.updateTime, novel.checkUpdateTime, novel.receiveUpdateTime
        )
        cache.saveChapters(novel, list)
        server.touchUpdate(novel)
        return list
    }

    /**
     * 询问服务器是否有更新，
     */
    fun askUpdate(novel: Novel) {
        debug { "askUpdate: <${novel.run { "$site.$author.$name.$receiveUpdateTime.$checkUpdateTime" }}>" }
        val result = server.askUpdate(novel) ?: return
        debug { "result: <${result.toJson()}}>" }
        if (result.chaptersCount ?: 0 > novel.chaptersCount) {
            debug { "has update ${result.chaptersCount} > ${novel.chaptersCount}" }
            // 如果有更新，也就是章节数比本地的多，就刷新章节列表，
            refreshChapters(novel)
        } else {
            debug { "no update ${result.chaptersCount} <= ${novel.chaptersCount}" }
            // 如果没更新，就保存服务器上的更新时间，如果更大的话，
            novel.apply {
                // 不更新receiveUpdateTime，不准，有时别人比较晚收到同一个更新然后推上去被拿到，
                checkUpdateTime = maxOf(checkUpdateTime, result.checkUpdateTime)
            }
        }
    }

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
            refreshChapters(localNovel)
            val newCount = localNovel.chaptersCount
            newCount > oldCount
        } else {
            false
        }
    }

    fun requestChapters(novel: Novel): List<NovelChapter> {
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        // 先读取缓存，
        cache.loadChapters(novel)?.also {
            return it
        }
        return refreshChapters(novel)
    }

    private fun requireNovelDetail(novel: Novel) {
        debug { "requireNovelDetail $novel" }
        // chapters非空表示已经获取过小说详情了，
        if (novel.chapters != null) {
            return
        }
        requestDetail(novel)
    }

    private fun requestDetail(novel: Novel) {
        api.updateNovelDetail(novel)
        // 写入数据库，包括名字作者和extra都以详情页返回结果为准，
        app.db.novelDao().updateNovelDetail(novel.nId,
                novel.name, novel.author, novel.detail,
                novel.image, novel.introduction, novel.updateTime, novel.nChapters)
    }

    fun getNovel(id: Long): Novel = app.query(id)

    fun requestDetail(id: Long): Novel {
        val novel = app.query(id)
        requestDetail(novel)
        return novel
    }

    fun getNovelDetail(id: Long): Novel {
        val novel = app.query(id)
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        return novel
    }

    fun getDetailUrl(novel: Novel): String =
            api.getDetailUrl(novel)

    fun getContentUrl(novel: Novel, chapter: NovelChapter): String =
            api.getContentUrl(novel, chapter)

    fun allNovelContexts() = api.contexts

    /**
     * 列出所有网站，
     */
    fun listSites(): List<Site> = app.db.siteDao().list()

    /**
     * 同步所有网站到数据库，app升级时调用一次就好，
     */
    fun syncSites(): List<Site> = app.db.runInTransaction<List<Site>> {
        allNovelContexts().map { context ->
            context.site.run {
                app.queryOrNewSite(name, baseUrl, logo, context.enabled)
            }.also { site ->
                if (site.baseUrl != context.site.baseUrl
                        || site.logo != context.site.logo) {
                    // 比如网站logo地址可能改了，
                    // 主要是有的网站logo是我发到百度外链的，可能被删除，
                    site.baseUrl = context.site.baseUrl
                    site.logo = context.site.logo
                    app.updateSiteInfo(site)
                }
            }
        }
    }

    /**
     * @param author 作者名为空就不从数据库查询，
     * @param block 回调，对每个网站搜索结果进行操作，
     */
    fun search(name: String, author: String?, block: (List<Novel>) -> Unit) {
        listSites().filter(Site::enabled).forEach {
            try {
                search(it.name, name, author).also(block)
            } catch (e: Exception) {
                val message = "搜索<${it.name}, $name, $author>失败，"
                Reporter.post(message, e)
                // 单个网站搜索失败不中断，
            }
        }
    }

    /**
     * @param author 作者名为空就不从数据库查询，
     */
    fun search(site: String, name: String, author: String?): List<Novel> {
        if (author != null) {
            // 如果有作者名，那结果只可能有一个，
            // 如果数据库里有了，就直接返回，
            app.query(site, author, name)?.also {
                return listOf(it)
            }
        }
        val context = api.getNovelContextByName(site)
        val resultList = api.search(context, name)
        val novelList = app.db.runInTransaction<List<Novel>> {
            resultList.map {
                // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
                app.queryOrNewNovel(NovelMinimal(it))
            }
        }
        return novelList.filter {
            // 过滤，author为空表示模糊搜索，只要小说名包含，
            // author不为空表示精确搜索，要小说名和作者名都匹配，
            if (author == null) {
                it.name.contains(name)
            } else {
                it.name == name && it.author == author
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

    fun getNovelFromUrl(site: String, url: String): Novel {
        return api.getNovelFromUrl(getNovelContextByName(site), url).let {
            // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
            app.queryOrNewNovel(NovelMinimal(it))
        }
    }

    fun removeWebViewCookies() = cookie.removeCookies()

    fun removeNovelContextCookies(site: String) = api.removeCookies(getNovelContextByName(site))

    /**
     * 从缓存中读小说正文，没有就返回空，用于导入小说，
     */
    fun getContent(novel: Novel, chapter: NovelChapter): List<String>? =
            cache.loadContent(novel, chapter)

    fun requestContent(novel: Novel, chapter: NovelChapter, refresh: Boolean): List<String> {
        // 指定刷新的话就不读缓存，
        if (!refresh) {
            cache.loadContent(novel, chapter)?.also {
                return it
            }
        }
        return api.getNovelContent(novel, chapter).also {
            // 缓存起来，
            cache.saveContent(novel, chapter, it)
        }
    }

    fun pinned(novel: Novel) = app.pinned(novel)

    fun cancelPinned(novel: Novel) = app.cancelPinned(novel)

    fun updateReadStatus(novel: Novel) = app.updateReadStatus(novel)

    /**
     * @return 返回该小说已经缓存的章节列表，
     * 考虑到缓存可能对key进行了单向加密，
     * 只用于contains判断特定章节是否已经缓存，不用于读取章节信息，
     */
    fun novelContentsCached(novel: Novel): Collection<String> = cache.novelContentCached(novel)

    fun siteEnabledChange(site: Site) = app.siteEnabledChange(site)

    fun history(historyCount: Int): List<Novel> = app.history(historyCount)

    fun getBookList(bookListId: Long): BookList = app.getBookList(bookListId)

    /**
     * 列表中的小说在该书单里的包含情况，
     */
    fun inBookList(bookListId: Long, list: List<Novel>) = app.inBookList(bookListId, list)

    fun addToBookList(bookListId: Long, novel: Novel) = app.addToBookList(bookListId, novel)
    fun removeFromBookList(bookListId: Long, novel: Novel) = app.removeFromBookList(bookListId, novel)

    fun getNovelFromBookList(bookListId: Long): List<Novel> = app.getNovelFromBookList(bookListId)
    fun getNovelMinimalFromBookList(bookListId: Long): List<NovelMinimal> = app.getNovelMinimalFromBookList(bookListId)
    fun allBookList() = app.allBookList()
    fun renameBookList(bookList: BookList, name: String) = app.renameBookList(bookList, name)
    fun removeBookList(bookList: BookList) = app.removeBookList(bookList)
    fun newBookList(name: String) = app.newBookList(name)

    /**
     * @throws IllegalArgumentException 不支持的地址直接抛异常，
     */
    fun getNovelFromUrl(url: String): Novel = api.getNovelFromUrl(api.getNovelContextByUrl(url), url).let {
        // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
        app.queryOrNewNovel(NovelMinimal(it))
    }

    fun importBookList(name: String, list: List<NovelMinimal>) = app.importBookList(name, list)
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
                novel.readAtChapterName = cache.loadChapters(novel)?.get(novel.readAtChapterIndex)?.name ?: ""
            }
            // 加入书架，
            novel.bookshelf = true
            // 不调用方法updateBookshelf，因为这个方法包含订阅更新推送，
            app.updateBookshelf(novel.nId, novel.bookshelf)
            // 普通更新阅读进度，比起来少了阅读时间，无所谓了，
            updateReadStatus(novel)
            novel
        }
        // 向极光订阅/取消对应tag,
        server.addTags(novelList)
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
            server.setTags(listBookshelf())

    fun removeAllCookies() {
        removeWebViewCookies()
        syncCookies(App.ctx)
        listSites().forEach {
            removeNovelContextCookies(it.name)
        }
    }
}
